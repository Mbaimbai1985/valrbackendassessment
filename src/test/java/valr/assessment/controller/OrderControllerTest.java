package valr.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import valr.assessment.dto.LimitOrderRequest;
import valr.assessment.model.Order;
import valr.assessment.enums.OrderSide;
import valr.assessment.enums.OrderStatus;
import valr.assessment.service.OrderBookService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private OrderBookService orderBookService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testSubmitLimitOrder() throws Exception {
        LimitOrderRequest request = new LimitOrderRequest(
            "buy", "BTCZAR", new BigDecimal("100000"), new BigDecimal("0.1"), null, null
        );
        
        Order mockOrder = new Order("BTCZAR", OrderSide.BUY, new BigDecimal("100000"), new BigDecimal("0.1"));
        mockOrder.setId("test-order-id");
        mockOrder.setCreatedTime(Instant.parse("2023-01-01T00:00:00Z"));
        
        when(orderBookService.submitOrder(any(Order.class))).thenReturn(mockOrder);
        
        mockMvc.perform(post("/api/v1/orders/limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-order-id"))
                .andExpect(jsonPath("$.side").value("buy"))
                .andExpect(jsonPath("$.currencyPair").value("BTCZAR"))
                .andExpect(jsonPath("$.price").value(100000))
                .andExpect(jsonPath("$.quantity").value(0.1))
                .andExpect(jsonPath("$.remainingQuantity").value(0.1))
                .andExpect(jsonPath("$.status").value("Open"));
    }
    
    @Test
    void testSubmitLimitOrderWithInvalidSide() throws Exception {
        LimitOrderRequest request = new LimitOrderRequest(
            "invalid", "BTCZAR", new BigDecimal("100000"), new BigDecimal("0.1"), null, null
        );
        
        mockMvc.perform(post("/api/v1/orders/limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSubmitLimitOrderWithMissingFields() throws Exception {
        LimitOrderRequest request = new LimitOrderRequest(
            null, "BTCZAR", new BigDecimal("100000"), new BigDecimal("0.1"), null, null
        );
        
        mockMvc.perform(post("/api/v1/orders/limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSubmitLimitOrderWithNegativePrice() throws Exception {
        LimitOrderRequest request = new LimitOrderRequest(
            "buy", "BTCZAR", new BigDecimal("-100000"), new BigDecimal("0.1"), null, null
        );
        
        mockMvc.perform(post("/api/v1/orders/limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSubmitLimitOrderWithZeroQuantity() throws Exception {
        LimitOrderRequest request = new LimitOrderRequest(
            "buy", "BTCZAR", new BigDecimal("100000"), BigDecimal.ZERO, null, null
        );
        
        mockMvc.perform(post("/api/v1/orders/limit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}