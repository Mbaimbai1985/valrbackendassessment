package valr.assessment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import valr.assessment.dto.OrderBookResponse;
import valr.assessment.service.OrderBookService;

@RestController
@RequestMapping("/api/v1")
public class OrderBookController {
    
    @Autowired
    private OrderBookService orderBookService;
    
    @GetMapping("/{currencyPair}/orderbook")
    public ResponseEntity<OrderBookResponse> getOrderBook(@PathVariable String currencyPair) {
        OrderBookResponse orderBook = orderBookService.getOrderBook(currencyPair);
        return ResponseEntity.ok(orderBook);
    }
}