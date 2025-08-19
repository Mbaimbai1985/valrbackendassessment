package valr.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import valr.assessment.enums.OrderSide;
import valr.assessment.model.Trade;
import valr.assessment.service.OrderBookService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeHistoryController.class)
class TradeHistoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private OrderBookService orderBookService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testGetTradeHistory() throws Exception {
        Trade trade1 = new Trade("BTCZAR", new BigDecimal("100000"), new BigDecimal("0.1"), 
                                OrderSide.BUY, "buy-id-1", "sell-id-1");
        trade1.setId("trade-1");
        trade1.setTradedAt(Instant.parse("2023-01-01T00:00:00Z"));
        
        Trade trade2 = new Trade("BTCZAR", new BigDecimal("101000"), new BigDecimal("0.05"), 
                                OrderSide.SELL, "buy-id-2", "sell-id-2");
        trade2.setId("trade-2");
        trade2.setTradedAt(Instant.parse("2023-01-01T00:01:00Z"));
        
        when(orderBookService.getRecentTrades(eq("BTCZAR"), eq(100)))
            .thenReturn(Arrays.asList(trade1, trade2));
        
        mockMvc.perform(get("/api/v1/BTCZAR/tradehistory")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].id").value("trade-1"))
                .andExpect(jsonPath("$[0].currencyPair").value("BTCZAR"))
                .andExpect(jsonPath("$[0].price").value("100000"))
                .andExpect(jsonPath("$[0].quantity").value("0.1"))
                .andExpect(jsonPath("$[0].takerSide").value("buy"))
                .andExpect(jsonPath("$[1].id").value("trade-2"));
    }
    
    @Test
    void testGetTradeHistoryWithLimit() throws Exception {
        Trade trade = new Trade("BTCZAR", new BigDecimal("100000"), new BigDecimal("0.1"), 
                               OrderSide.BUY, "buy-id", "sell-id");
        
        when(orderBookService.getRecentTrades(eq("BTCZAR"), eq(50)))
            .thenReturn(Collections.singletonList(trade));
        
        mockMvc.perform(get("/api/v1/BTCZAR/tradehistory?limit=50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    void testGetTradeHistoryEmpty() throws Exception {
        when(orderBookService.getRecentTrades(eq("BTCZAR"), eq(100)))
            .thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/v1/BTCZAR/tradehistory")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}