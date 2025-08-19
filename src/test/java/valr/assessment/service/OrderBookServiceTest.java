package valr.assessment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import valr.assessment.dto.OrderBookResponse;
import valr.assessment.model.Order;
import valr.assessment.enums.OrderSide;
import valr.assessment.enums.OrderStatus;
import valr.assessment.model.Trade;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookServiceTest {
    
    private OrderBookService orderBookService;
    
    @BeforeEach
    void setUp() {
        orderBookService = new OrderBookService();
        orderBookService.clearOrderBook();
    }
    
    @Test
    void testSubmitBuyOrderWithoutMatching() {
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        
        Order result = orderBookService.submitOrder(buyOrder);
        
        assertEquals(OrderStatus.OPEN, result.getStatus());
        assertEquals(0, result.getRemainingQuantity().compareTo(new BigDecimal("0.1")));
        
        OrderBookResponse orderBook = orderBookService.getOrderBook("BTCZAR");
        assertEquals(1, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
        assertEquals("0.1", orderBook.getBids().get(0).getQuantity());
        assertEquals("100000", orderBook.getBids().get(0).getPrice());
    }
    
    @Test
    void testSubmitSellOrderWithoutMatching() {
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("101000"), new BigDecimal("0.1"));
        
        Order result = orderBookService.submitOrder(sellOrder);
        
        assertEquals(OrderStatus.OPEN, result.getStatus());
        assertEquals(0, result.getRemainingQuantity().compareTo(new BigDecimal("0.1")));
        
        OrderBookResponse orderBook = orderBookService.getOrderBook("BTCZAR");
        assertEquals(0, orderBook.getBids().size());
        assertEquals(1, orderBook.getAsks().size());
        assertEquals("0.1", orderBook.getAsks().get(0).getQuantity());
        assertEquals("101000", orderBook.getAsks().get(0).getPrice());
    }
    
    @Test
    void testCompleteMatchBuyTakesAsk() {
        // Place sell order first
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.1"));
        orderBookService.submitOrder(sellOrder);
        
        // Place matching buy order
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order result = orderBookService.submitOrder(buyOrder);
        
        // Both orders should be filled
        assertEquals(OrderStatus.FILLED, result.getStatus());
        assertEquals(0, result.getRemainingQuantity().compareTo(BigDecimal.ZERO));
        assertEquals(OrderStatus.FILLED, sellOrder.getStatus());
        assertEquals(0, sellOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO));
        
        // Order book should be empty
        OrderBookResponse orderBook = orderBookService.getOrderBook("BTCZAR");
        assertEquals(0, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
        
        // Should have one trade
        List<Trade> trades = orderBookService.getRecentTrades("BTCZAR", 10);
        assertEquals(1, trades.size());
        assertEquals(0, trades.get(0).getPrice().compareTo(new BigDecimal("100000")));
        assertEquals(0, trades.get(0).getQuantity().compareTo(new BigDecimal("0.1")));
        assertEquals("buy", trades.get(0).getTakerSide());
    }
    
    @Test
    void testCompleteMatchSellTakesBid() {
        // Place buy order first
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        orderBookService.submitOrder(buyOrder);
        
        // Place matching sell order
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order result = orderBookService.submitOrder(sellOrder);
        
        // Both orders should be filled
        assertEquals(OrderStatus.FILLED, result.getStatus());
        assertEquals(0, result.getRemainingQuantity().compareTo(BigDecimal.ZERO));
        assertEquals(OrderStatus.FILLED, buyOrder.getStatus());
        assertEquals(0, buyOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO));
        
        // Should have one trade with sell as taker
        List<Trade> trades = orderBookService.getRecentTrades("BTCZAR", 10);
        assertEquals(1, trades.size());
        assertEquals("sell", trades.get(0).getTakerSide());
    }
    
    @Test
    void testPartialMatchLargerBuyOrder() {
        // Place small sell order
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.05"));
        orderBookService.submitOrder(sellOrder);
        
        // Place larger buy order
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order result = orderBookService.submitOrder(buyOrder);
        
        // Sell order should be filled, buy order partially filled
        assertEquals(OrderStatus.FILLED, sellOrder.getStatus());
        assertEquals(OrderStatus.PARTIALLY_FILLED, result.getStatus());
        assertEquals(0, result.getRemainingQuantity().compareTo(new BigDecimal("0.05")));
        
        // Order book should have remaining buy order
        OrderBookResponse orderBook = orderBookService.getOrderBook("BTCZAR");
        assertEquals(1, orderBook.getBids().size());
        assertEquals(0, orderBook.getAsks().size());
        assertEquals("0.05", orderBook.getBids().get(0).getQuantity());
        
        // Should have one trade
        List<Trade> trades = orderBookService.getRecentTrades("BTCZAR", 10);
        assertEquals(1, trades.size());
        assertEquals(0, trades.get(0).getQuantity().compareTo(new BigDecimal("0.05")));
    }
    
    @Test
    void testMultipleOrderMatching() {
        // Place multiple sell orders at different prices
        Order sellOrder1 = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.05"));
        Order sellOrder2 = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("101000"), new BigDecimal("0.05"));
        orderBookService.submitOrder(sellOrder1);
        orderBookService.submitOrder(sellOrder2);
        
        // Place large buy order that matches both
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("101000"), new BigDecimal("0.1"));
        Order result = orderBookService.submitOrder(buyOrder);
        
        // All orders should be filled
        assertEquals(OrderStatus.FILLED, result.getStatus());
        assertEquals(OrderStatus.FILLED, sellOrder1.getStatus());
        assertEquals(OrderStatus.FILLED, sellOrder2.getStatus());
        
        // Should have two trades
        List<Trade> trades = orderBookService.getRecentTrades("BTCZAR", 10);
        assertEquals(2, trades.size());
        
        // First trade should be at better price (100000)
        Trade firstTrade = trades.stream()
            .filter(t -> t.getPrice().compareTo(new BigDecimal("100000")) == 0)
            .findFirst()
            .orElse(null);
        assertNotNull(firstTrade);
        assertEquals(0, firstTrade.getQuantity().compareTo(new BigDecimal("0.05")));
    }
    
    @Test
    void testPricePriority() {
        // Place sell orders at different prices
        Order sellOrder1 = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("101000"), new BigDecimal("0.1"));
        Order sellOrder2 = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.1"));
        orderBookService.submitOrder(sellOrder1);
        orderBookService.submitOrder(sellOrder2);
        
        OrderBookResponse orderBook = orderBookService.getOrderBook("BTCZAR");
        
        // Lower price should be first in asks
        assertEquals("100000", orderBook.getAsks().get(0).getPrice());
        assertEquals("101000", orderBook.getAsks().get(1).getPrice());
    }
    
    @Test
    void testBidPriceOrdering() {
        // Place buy orders at different prices
        Order buyOrder1 = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("99000"), new BigDecimal("0.1"));
        Order buyOrder2 = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        orderBookService.submitOrder(buyOrder1);
        orderBookService.submitOrder(buyOrder2);
        
        OrderBookResponse orderBook = orderBookService.getOrderBook("BTCZAR");
        
        // Higher price should be first in bids
        assertEquals("100000", orderBook.getBids().get(0).getPrice());
        assertEquals("99000", orderBook.getBids().get(1).getPrice());
    }
    
    @Test
    void testGetAllOpenOrders() {
        Order openOrder1 = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order openOrder2 = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("101000"), new BigDecimal("0.1"));
        
        orderBookService.submitOrder(openOrder1);
        orderBookService.submitOrder(openOrder2);
        
        List<Order> openOrders = orderBookService.getAllOpenOrders();
        assertEquals(2, openOrders.size());
        
        // Now fill one order
        Order fillOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("101000"), new BigDecimal("0.1"));
        orderBookService.submitOrder(fillOrder);
        
        openOrders = orderBookService.getAllOpenOrders();
        assertEquals(1, openOrders.size());
        assertEquals(openOrder1.getId(), openOrders.get(0).getId());
    }
    
    @Test
    void testGetOrderById() {
        Order order = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        orderBookService.submitOrder(order);
        
        assertTrue(orderBookService.getOrder(order.getId()).isPresent());
        assertFalse(orderBookService.getOrder("non-existent").isPresent());
    }
    
    @Test
    void testTradeHistoryLimit() {
        // Create multiple trades
        for (int i = 0; i < 5; i++) {
            Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.01"));
            Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.01"));
            orderBookService.submitOrder(sellOrder);
            orderBookService.submitOrder(buyOrder);
        }
        
        List<Trade> allTrades = orderBookService.getRecentTrades("BTCZAR", 10);
        assertEquals(5, allTrades.size());
        
        List<Trade> limitedTrades = orderBookService.getRecentTrades("BTCZAR", 3);
        assertEquals(3, limitedTrades.size());
    }
    
    @Test
    void testClearOrderBook() {
        Order order = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        orderBookService.submitOrder(order);
        
        assertFalse(orderBookService.getAllOpenOrders().isEmpty());
        
        orderBookService.clearOrderBook();
        
        assertTrue(orderBookService.getAllOpenOrders().isEmpty());
        OrderBookResponse orderBook = orderBookService.getOrderBook("BTCZAR");
        assertTrue(orderBook.getBids().isEmpty());
        assertTrue(orderBook.getAsks().isEmpty());
    }
}