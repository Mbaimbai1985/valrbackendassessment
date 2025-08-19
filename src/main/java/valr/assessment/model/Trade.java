package valr.assessment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import valr.assessment.enums.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private String id;
    private String currencyPair;
    private BigDecimal price;
    private BigDecimal quantity;
    private String takerSide;
    private Instant tradedAt;
    private String buyOrderId;
    private String sellOrderId;
    
    public Trade(String currencyPair, BigDecimal price, BigDecimal quantity, 
                 OrderSide takerSide, String buyOrderId, String sellOrderId) {
        this.id = UUID.randomUUID().toString();
        this.currencyPair = currencyPair;
        this.price = price;
        this.quantity = quantity;
        this.takerSide = takerSide.getValue();
        this.tradedAt = Instant.now();
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
    }
}