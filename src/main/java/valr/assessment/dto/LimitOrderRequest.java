package valr.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitOrderRequest {
    @NotNull
    private String side;
    
    @NotNull
    private String currencyPair;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    @NotNull
    @Positive
    private BigDecimal quantity;
    
    private String postOnly;
    private String customerOrderId;
}