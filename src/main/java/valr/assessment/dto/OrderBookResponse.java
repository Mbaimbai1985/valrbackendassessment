package valr.assessment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookResponse {
    @JsonProperty("Asks")
    private List<OrderBookEntry> asks;
    
    @JsonProperty("Bids")
    private List<OrderBookEntry> bids;
    
    @JsonProperty("LastChange")
    private String lastChange;
    
    @JsonProperty("SequenceNumber")
    private long sequenceNumber;
}