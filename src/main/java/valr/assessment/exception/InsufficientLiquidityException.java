package valr.assessment.exception;

public class InsufficientLiquidityException extends RuntimeException {
    public InsufficientLiquidityException(String message) {
        super(message);
    }
    
    public InsufficientLiquidityException(String message, Throwable cause) {
        super(message, cause);
    }
}