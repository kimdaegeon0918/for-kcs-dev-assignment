package com.example.demo.integration;

import com.example.demo.entity.Company;
import com.example.demo.entity.StocksHistory;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.StocksHistoryRepository;
import com.example.demo.service.StockHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockHistoryService stockHistoryService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StocksHistoryRepository stocksHistoryRepository;

    @BeforeEach
    public void setup() {
        Company apple = new Company("AAPL", "Apple Inc.");
        Company google = new Company("GOOGL", "Google Inc.");
        companyRepository.save(apple);
        companyRepository.save(google);

        StocksHistory appleStock = new StocksHistory(1, "AAPL", LocalDate.of(2024, 1, 1), 150L);
        StocksHistory googleStock = new StocksHistory(2, "GOOGL", LocalDate.of(2024, 1, 1), 100L);
        stocksHistoryRepository.save(appleStock);
        stocksHistoryRepository.save(googleStock);
    }

    // 단일 회사의 주식 정보가 올바르게 조회되는지 테스트합니다.
    @Test
    public void testGetCompanyByCode() {
        // Given
        String companyCode = "AAPL";

        // When
        Company company = stockHistoryService.getCompanyByCode(companyCode);

        // Then
        assertThat(company).isNotNull();
        assertThat(company.getCompanyCode()).isEqualTo(companyCode);
        assertThat(company.getCompanyName()).isEqualTo("Apple Inc.");

        System.out.println("ApplicationIntegrationTest.testGetCompanyByCode: SUCCESS");
    }

    // 주어진 회사 코드와 날짜 범위에 해당하는 주식 히스토리가 올바르게 조회되는지 테스트합니다.
    @Test
    public void testGetStocksHistory() {
        // Given
        String companyCode = "AAPL";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // When
        List<StocksHistory> stocksHistory = stockHistoryService.getStocksHistory(companyCode, startDate, endDate);

        // Then
        assertThat(stocksHistory).isNotEmpty();
        assertThat(stocksHistory.get(0).getCompanyCode()).isEqualTo(companyCode);
        assertThat(stocksHistory.get(0).getTradeDate()).isEqualTo(startDate);
        assertThat(stocksHistory.get(0).getClosePrice()).isEqualTo(150L);

        System.out.println("ApplicationIntegrationTest.testGetStocksHistory: SUCCESS");
    }

    //단일 회사의 주식 정보를 API를 통해 JSON 형식으로 조회하는 기능을 테스트합니다.
    @Test
    public void testGetSingleStockApi() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/single")
                        .param("companyCode", "AAPL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .header("x-api-key", "c18aa07f-f005-4c2f-b6db-dff8294e6b5e"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.AAPL[0].companyName").value("Apple Inc."))
                .andExpect(jsonPath("$.data.AAPL[0].tradeDate").value("2024-01-01"))
                .andExpect(jsonPath("$.data.AAPL[0].closingPrice").value(150L));

        System.out.println("ApplicationIntegrationTest.testGetSingleStockApi: SUCCESS");
    }

    // 여러 회사의 주식 정보를 API를 통해 JSON 형식으로 조회하는 기능을 테스트합니다.
    @Test
    public void testGetMultipleStocksApi() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/multiple")
                        .param("companyCode", "AAPL")
                        .param("companyCode", "GOOGL")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .header("x-api-key", "c18aa07f-f005-4c2f-b6db-dff8294e6b5e"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.AAPL[0].companyName").value("Apple Inc."))
                .andExpect(jsonPath("$.data.GOOGL[0].companyName").value("Google Inc."));

        System.out.println("ApplicationIntegrationTest.testGetMultipleStocksApi: SUCCESS");
    }
}
