package com.example.demo.controller;

import com.example.demo.entity.Company;
import com.example.demo.entity.StocksHistory;
import com.example.demo.service.StockHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(StockHistoryController.class)
public class StockHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockHistoryService stockService;

    @Value("${api.key}")
    private String API_KEY;

    private Company testCompany;
    private StocksHistory testStocksHistory;

    @BeforeEach
    public void setup() {
        testCompany = new Company("AAPL", "Apple Inc.");
        testStocksHistory = new StocksHistory(1, "AAPL", LocalDate.of(2024, 1, 1), 150L);
    }

    // 단일회사 JSON형식 요청 성공
    @Test
    public void testGetSingleStock_Success_JSON() throws Exception {
        when(stockService.getCompanyByCode(anyString())).thenReturn(testCompany);
        when(stockService.getStocksHistory(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testStocksHistory));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stocks/single")
                        .param("companyCode", "AAPL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("format", "json")
                        .header("x-api-key", API_KEY))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.AAPL[0].companyName").value("Apple Inc."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.AAPL[0].tradeDate").value("2024-01-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.AAPL[0].closingPrice").value(150));
    }

    // 단일회사 XML형식 성공
    @Test
    public void testGetSingleStock_Success_XML() throws Exception {
        when(stockService.getCompanyByCode(anyString())).thenReturn(testCompany);
        when(stockService.getStocksHistory(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testStocksHistory));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stocks/single")
                        .param("companyCode", "AAPL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("format", "xml")
                        .header("x-api-key", API_KEY))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/xml"))
                .andExpect(MockMvcResultMatchers.xpath("/ApiResponse/status").string("success"))
                .andExpect(MockMvcResultMatchers.xpath("/ApiResponse/data/AAPL/companyName").string("Apple Inc."))
                .andExpect(MockMvcResultMatchers.xpath("/ApiResponse/data/AAPL/tradeDate").string("2024-01-01"))
                .andExpect(MockMvcResultMatchers.xpath("/ApiResponse/data/AAPL/closingPrice").string("150"));
    }

    // 단일회사 유효하지않은 API키
    @Test
    public void testGetSingleStock_InvalidApiKey() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stocks/single")
                        .param("companyCode", "AAPL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("format", "json")
                        .header("x-api-key", "invalid-api-key"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid API Key"));
    }

    // 다중회사 JSON형식 성공
    @Test
    public void testGetMultipleStocks_Success_JSON() throws Exception {
        when(stockService.getCompanyByCode("AAPL")).thenReturn(testCompany);
        when(stockService.getCompanyByCode("GOOGL")).thenReturn(new Company("GOOGL", "Google Inc."));
        when(stockService.getStocksHistory(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testStocksHistory));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stocks/multiple")
                        .param("companyCode", "AAPL")
                        .param("companyCode", "GOOGL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("format", "json")
                        .header("x-api-key", API_KEY))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.AAPL[0].companyName").value("Apple Inc."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.GOOGL[0].companyName").value("Google Inc."));
    }

    // 다중회사 XML형식 성공
    @Test
    public void testGetMultipleStocks_Success_XML() throws Exception {
        when(stockService.getCompanyByCode("AAPL")).thenReturn(testCompany);
        when(stockService.getCompanyByCode("GOOGL")).thenReturn(new Company("GOOGL", "Google Inc."));
        when(stockService.getStocksHistory(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testStocksHistory));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stocks/multiple")
                        .param("companyCode", "AAPL")
                        .param("companyCode", "GOOGL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("format", "xml")
                        .header("x-api-key", API_KEY))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/xml"))
                .andExpect(MockMvcResultMatchers.xpath("/ApiResponse/status").string("success"))
                .andExpect(MockMvcResultMatchers.xpath("/ApiResponse/data/AAPL/companyName").string("Apple Inc."))
                .andExpect(MockMvcResultMatchers.xpath("/ApiResponse/data/GOOGL/companyName").string("Google Inc."));
    }

    // 다중회사 API키 누락
    @Test
    public void testGetMultipleStocks_MissingApiKey() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stocks/multiple")
                        .param("companyCode", "AAPL")
                        .param("companyCode", "GOOGL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("format", "json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("API Key is missing"));
    }

    // 시작날짜가 종료날짜보다 나중일때
    @Test
    public void testGetStocks_StartDateAfterEndDate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stocks/single")
                        .param("companyCode", "AAPL")
                        .param("startDate", "2024-02-01")
                        .param("endDate", "2024-01-01")
                        .param("format", "json")
                        .header("x-api-key", API_KEY))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("startDate cannot be after endDate"));
    }
}
