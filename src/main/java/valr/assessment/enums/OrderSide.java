package valr.assessment.enums;

public enum OrderSide {
    BUY("buy"),
    SELL("sell");
    
    private final String value;
    
    OrderSide(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static OrderSide fromString(String text) {
        for (OrderSide side : OrderSide.values()) {
            if (side.value.equalsIgnoreCase(text)) {
                return side;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}