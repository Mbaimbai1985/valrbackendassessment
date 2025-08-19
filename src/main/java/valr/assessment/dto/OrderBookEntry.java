package valr.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookEntry {
    private String side;
    private String quantity;
    private String price;
    private String currencyPair;
    private int orderCount;
}