package valr.assessment.model;

import org.junit.jupiter.api.Test;
import valr.assessment.enums.OrderSide;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TradeTest {
    
    @Test
    void testTradeCreationWithBuyTaker() {
        Trade trade = new Trade(
            "BTCZAR",
            new BigDecimal("100000"),
            new BigDecimal("0.1"),
            OrderSide.BUY,
            "buy-order-id",
            "sell-order-id"
        );
        
        assertNotNull(trade.getId());
        assertEquals("BTCZAR", trade.getCurrencyPair());
        assertEquals(new BigDecimal("100000"), trade.getPrice());
        assertEquals(new BigDecimal("0.1"), trade.getQuantity());
        assertEquals("buy", trade.getTakerSide());
        assertEquals("buy-order-id", trade.getBuyOrderId());
        assertEquals("sell-order-id", trade.getSellOrderId());
        assertNotNull(trade.getTradedAt());
    }
    
    @Test
    void testTradeCreationWithSellTaker() {
        Trade trade = new Trade(
            "BTCZAR",
            new BigDecimal("100000"),
            new BigDecimal("0.1"),
            OrderSide.SELL,
            "buy-order-id",
            "sell-order-id"
        );
        
        assertEquals("sell", trade.getTakerSide());
    }
}