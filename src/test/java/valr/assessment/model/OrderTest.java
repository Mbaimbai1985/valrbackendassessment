package valr.assessment.model;

import org.junit.jupiter.api.Test;
import valr.assessment.enums.OrderSide;
import valr.assessment.enums.OrderStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    
    @Test
    void testOrderCreation() {
        Order order = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        
        assertNotNull(order.getId());
        assertEquals("BTCZAR", order.getCurrencyPair());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(new BigDecimal("100000"), order.getPrice());
        assertEquals(new BigDecimal("0.1"), order.getQuantity());
        assertEquals(new BigDecimal("0.1"), order.getRemainingQuantity());
        assertEquals(OrderStatus.OPEN, order.getStatus());
        assertNotNull(order.getCreatedTime());
    }
    
    @Test
    void testIsFilledWhenRemainingQuantityIsZero() {
        Order order = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        order.setRemainingQuantity(BigDecimal.ZERO);
        
        assertTrue(order.isFilled());
    }
    
    @Test
    void testIsNotFilledWhenRemainingQuantityIsPositive() {
        Order order = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        
        assertFalse(order.isFilled());
    }
    
    @Test
    void testBuyOrderCanMatchSellOrderAtLowerPrice() {
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("99000"), new BigDecimal("0.1"));
        
        assertTrue(buyOrder.canMatch(sellOrder));
    }
    
    @Test
    void testBuyOrderCanMatchSellOrderAtSamePrice() {
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("100000"), new BigDecimal("0.1"));
        
        assertTrue(buyOrder.canMatch(sellOrder));
    }
    
    @Test
    void testBuyOrderCannotMatchSellOrderAtHigherPrice() {
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("101000"), new BigDecimal("0.1"));
        
        assertFalse(buyOrder.canMatch(sellOrder));
    }
    
    @Test
    void testSellOrderCanMatchBuyOrderAtHigherPrice() {
        Order sellOrder = new Order("BTCZAR", OrderSide.SELL, new BigDecimal("99000"), new BigDecimal("0.1"));
        Order buyOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        
        assertTrue(sellOrder.canMatch(buyOrder));
    }
    
    @Test
    void testOrdersWithSameSideCannotMatch() {
        Order buyOrder1 = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        Order buyOrder2 = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("99000"), new BigDecimal("0.1"));
        
        assertFalse(buyOrder1.canMatch(buyOrder2));
    }
}