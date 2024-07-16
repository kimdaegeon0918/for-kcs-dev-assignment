package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.entity.StocksHistory;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.StocksHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
public class StockHistoryServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private StocksHistoryRepository stocksHistoryRepository;

    @InjectMocks
    private StockHistoryService stockHistoryService;

    private Company testCompany;
    private StocksHistory testStocksHistory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        testCompany = new Company("AAPL", "Apple Inc.");
        testStocksHistory = new StocksHistory(1, "AAPL", LocalDate.of(2024, 1, 1), 150L);
    }

    @Test
    public void testGetCompanyByCode() {
        // given
        when(companyRepository.findByCompanyCode(anyString())).thenReturn(testCompany);

        // when
        Company company = stockHistoryService.getCompanyByCode("AAPL");

        // then
        assertThat(company).isNotNull();
        assertThat(company.getCompanyCode()).isEqualTo("AAPL");
        assertThat(company.getCompanyName()).isEqualTo("Apple Inc.");

        System.out.println("StockHistoryServiceTest.testGetCompanyByCode: SUCCESS");
    }

    @Test
    public void testGetStocksHistory() {
        // given
        when(stocksHistoryRepository.findByCompanyCodeAndTradeDateBetween(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testStocksHistory));

        // when
        List<StocksHistory> stocksHistory = stockHistoryService.getStocksHistory("AAPL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        // then
        assertThat(stocksHistory).isNotEmpty();
        assertThat(stocksHistory.get(0).getCompanyCode()).isEqualTo("AAPL");
        assertThat(stocksHistory.get(0).getTradeDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(stocksHistory.get(0).getClosePrice()).isEqualTo(150L);

        System.out.println("StockHistoryServiceTest.testGetStocksHistory: SUCCESS");
    }
}
