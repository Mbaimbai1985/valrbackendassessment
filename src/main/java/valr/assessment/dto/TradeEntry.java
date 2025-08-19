package valr.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntry {
    private String price;
    private String quantity;
    private String currencyPair;
    private Instant tradedAt;
    private String takerSide;
    private double sequenceId;
    private String id;
    private String quoteVolume;
}