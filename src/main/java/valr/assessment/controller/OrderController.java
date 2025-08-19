package valr.assessment.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import valr.assessment.dto.LimitOrderRequest;
import valr.assessment.dto.LimitOrderResponse;
import valr.assessment.model.Order;
import valr.assessment.enums.OrderSide;
import valr.assessment.service.OrderBookService;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    @Autowired
    private OrderBookService orderBookService;
    
    @PostMapping("/limit")
    public ResponseEntity<LimitOrderResponse> submitLimitOrder(@Valid @RequestBody LimitOrderRequest request) {
        try {
            Order order = new Order(
                request.getCurrencyPair(),
                OrderSide.fromString(request.getSide()),
                request.getPrice(),
                request.getQuantity()
            );
            Order processedOrder = orderBookService.submitOrder(order);
            LimitOrderResponse response = new LimitOrderResponse(
                processedOrder.getId(),
                processedOrder.getSide().getValue(),
                processedOrder.getCurrencyPair(),
                processedOrder.getPrice(),
                processedOrder.getQuantity(),
                processedOrder.getRemainingQuantity(),
                processedOrder.getStatus().getValue(),
                processedOrder.getCreatedTime()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}