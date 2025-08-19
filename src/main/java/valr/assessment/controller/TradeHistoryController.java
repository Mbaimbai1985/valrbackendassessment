package valr.assessment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import valr.assessment.dto.TradeHistoryResponse;
import valr.assessment.dto.TradeEntry;
import valr.assessment.model.Trade;
import valr.assessment.service.OrderBookService;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class TradeHistoryController {
    
    @Autowired
    private OrderBookService orderBookService;
    private final AtomicLong sequenceCounter = new AtomicLong(1);
    
    @GetMapping("/{currencyPair}/tradehistory")
    public ResponseEntity<List<TradeEntry>> getTradeHistory(
            @PathVariable String currencyPair,
            @RequestParam(defaultValue = "100") int limit) {
        
        List<Trade> trades = orderBookService.getRecentTrades(currencyPair, limit);
        
        List<TradeEntry> tradeEntries = trades.stream()
            .map(trade -> {
                String quoteVolume = trade.getPrice().multiply(trade.getQuantity()).toPlainString();
                
                return new TradeEntry(
                    trade.getPrice().toPlainString(),
                    trade.getQuantity().toPlainString(),
                    trade.getCurrencyPair(),
                    trade.getTradedAt(),
                    trade.getTakerSide(),
                    sequenceCounter.incrementAndGet(),
                    trade.getId(),
                    quoteVolume
                );
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(tradeEntries);
    }
}