package valr.assessment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import valr.assessment.dto.OrderBookResponse;
import valr.assessment.dto.OrderBookEntry;
import valr.assessment.model.Order;
import valr.assessment.enums.OrderSide;
import valr.assessment.enums.OrderStatus;
import valr.assessment.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderBookService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderBookService.class);
    private final TreeMap<BigDecimal, LinkedList<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<BigDecimal, LinkedList<Order>> asks = new TreeMap<>();
    private final Map<String, Order> allOrders = new ConcurrentHashMap<>();
    private final List<Trade> tradeHistory = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong sequenceNumber = new AtomicLong(1);
    
    public synchronized Order submitOrder(Order order) {
        log.info("Submitting order: {}", order);
        allOrders.put(order.getId(), order);
        List<Trade> trades = matchOrder(order);
        tradeHistory.addAll(trades);
        if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            addOrderToBook(order);
        }
        updateOrderStatus(order);
        
        log.info("Order processed: {} with {} trades", order.getId(), trades.size());
        return order;
    }
    
    private List<Trade> matchOrder(Order incomingOrder) {
        List<Trade> trades = new ArrayList<>();
        
        TreeMap<BigDecimal, LinkedList<Order>> oppositeBook = 
            incomingOrder.getSide() == OrderSide.BUY ? asks : bids;
        
        Iterator<Map.Entry<BigDecimal, LinkedList<Order>>> priceIterator = 
            oppositeBook.entrySet().iterator();
        
        while (priceIterator.hasNext() && 
               incomingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            
            Map.Entry<BigDecimal, LinkedList<Order>> priceLevel = priceIterator.next();
            BigDecimal price = priceLevel.getKey();
            LinkedList<Order> ordersAtPrice = priceLevel.getValue();
            if (incomingOrder.getSide() == OrderSide.BUY && incomingOrder.getPrice().compareTo(price) < 0) {
                break;
            }
            if (incomingOrder.getSide() == OrderSide.SELL && incomingOrder.getPrice().compareTo(price) > 0) {
                break;
            }
            
            Iterator<Order> orderIterator = ordersAtPrice.iterator();
            while (orderIterator.hasNext() && 
                   incomingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                
                Order matchingOrder = orderIterator.next();
                BigDecimal tradeQuantity = incomingOrder.getRemainingQuantity()
                    .min(matchingOrder.getRemainingQuantity());
                Trade trade = createTrade(incomingOrder, matchingOrder, tradeQuantity, matchingOrder.getPrice());
                trades.add(trade);
                incomingOrder.setRemainingQuantity(
                    incomingOrder.getRemainingQuantity().subtract(tradeQuantity));
                matchingOrder.setRemainingQuantity(
                    matchingOrder.getRemainingQuantity().subtract(tradeQuantity));
                updateOrderStatus(incomingOrder);
                updateOrderStatus(matchingOrder);
                if (matchingOrder.isFilled()) {
                    orderIterator.remove();
                }
                
                log.info("Trade executed: {} @ {} for quantity {}", 
                    trade.getId(), price, tradeQuantity);
            }
            if (ordersAtPrice.isEmpty()) {
                priceIterator.remove();
            }
        }
        
        return trades;
    }
    
    private Trade createTrade(Order incomingOrder, Order matchingOrder, 
                             BigDecimal quantity, BigDecimal price) {
        String buyOrderId = incomingOrder.getSide() == OrderSide.BUY ? 
            incomingOrder.getId() : matchingOrder.getId();
        String sellOrderId = incomingOrder.getSide() == OrderSide.SELL ? 
            incomingOrder.getId() : matchingOrder.getId();
        
        return new Trade(
            incomingOrder.getCurrencyPair(),
            price,
            quantity,
            incomingOrder.getSide(),
            buyOrderId,
            sellOrderId
        );
    }
    
    private void addOrderToBook(Order order) {
        TreeMap<BigDecimal, LinkedList<Order>> book = 
            order.getSide() == OrderSide.BUY ? bids : asks;
        
        book.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).addLast(order);
    }
    
    private void updateOrderStatus(Order order) {
        if (order.isFilled()) {
            order.setStatus(OrderStatus.FILLED);
        } else if (!order.getRemainingQuantity().equals(order.getQuantity())) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
    }
    
    public OrderBookResponse getOrderBook(String currencyPair) {
        List<OrderBookEntry> bidEntries = createOrderBookEntries(bids, "buy");
        List<OrderBookEntry> askEntries = createOrderBookEntries(asks, "sell");
        
        return new OrderBookResponse(
            askEntries,
            bidEntries,
            Instant.now().toString(),
            sequenceNumber.incrementAndGet()
        );
    }
    
    private List<OrderBookEntry> createOrderBookEntries(
            TreeMap<BigDecimal, LinkedList<Order>> book, String side) {
        
        return book.entrySet().stream()
            .map(entry -> {
                BigDecimal price = entry.getKey();
                List<Order> orders = entry.getValue();
                
                BigDecimal totalQuantity = orders.stream()
                    .map(Order::getRemainingQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return new OrderBookEntry(
                    side,
                    totalQuantity.toPlainString(),
                    price.toPlainString(),
                    orders.get(0).getCurrencyPair(),
                    orders.size()
                );
            })
            .collect(Collectors.toList());
    }
    
    public List<Trade> getRecentTrades(String currencyPair, int limit) {
        return tradeHistory.stream()
            .filter(trade -> trade.getCurrencyPair().equals(currencyPair))
            .sorted((t1, t2) -> t2.getTradedAt().compareTo(t1.getTradedAt()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<Order> getAllOpenOrders() {
        return allOrders.values().stream()
            .filter(order -> order.getStatus() == OrderStatus.OPEN || 
                           order.getStatus() == OrderStatus.PARTIALLY_FILLED)
            .collect(Collectors.toList());
    }
    
    public Optional<Order> getOrder(String orderId) {
        return Optional.ofNullable(allOrders.get(orderId));
    }
    public void clearOrderBook() {
        bids.clear();
        asks.clear();
        allOrders.clear();
        tradeHistory.clear();
        sequenceNumber.set(1);
    }
}