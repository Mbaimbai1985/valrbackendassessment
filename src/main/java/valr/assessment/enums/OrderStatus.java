package valr.assessment.enums;

public enum OrderStatus {
    OPEN("Open"),
    PARTIALLY_FILLED("Partially Filled"),
    FILLED("Filled"),
    CANCELLED("Cancelled");
    
    private final String value;
    
    OrderStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}