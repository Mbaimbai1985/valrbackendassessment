package valr.assessment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import valr.assessment.enums.OrderSide;
import valr.assessment.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String currencyPair;
    private OrderSide side;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal remainingQuantity;
    private OrderStatus status;
    private Instant createdTime;
    
    public Order(String currencyPair, OrderSide side, BigDecimal price, BigDecimal quantity) {
        this.id = UUID.randomUUID().toString();
        this.currencyPair = currencyPair;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.OPEN;
        this.createdTime = Instant.now();
    }
    
    public boolean isFilled() {
        return remainingQuantity.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean canMatch(Order other) {
        if (this.side == other.side) {
            return false;
        }
        
        if (this.side == OrderSide.BUY) {
            return this.price.compareTo(other.price) >= 0;
        } else {
            return this.price.compareTo(other.price) <= 0;
        }
    }
}