package com.example.demo.repository;

import com.example.demo.entity.Company;
import com.example.demo.entity.StocksHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
public class StocksHistoryRepositoryTest {

    @Autowired
    private StocksHistoryRepository stocksHistoryRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    public void testFindByCompanyCodeAndTradeDateBetween() {
        // Given
        Company company = new Company("AAPL", "Apple Inc.");
        companyRepository.save(company);

        StocksHistory stock = new StocksHistory(1, "AAPL", LocalDate.of(2024, 1, 1), 150L);
        stocksHistoryRepository.save(stock);

        // When
        List<StocksHistory> stocks = stocksHistoryRepository.findByCompanyCodeAndTradeDateBetween(
                "AAPL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        // Then
        assertThat(stocks).isNotEmpty();
        assertThat(stocks.get(0).getCompanyCode()).isEqualTo("AAPL");
        assertThat(stocks.get(0).getTradeDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(stocks.get(0).getClosePrice()).isEqualTo(150L);

        System.out.println("StocksHistoryRepositoryTest.testFindByCompanyCodeAndTradeDateBetween: SUCCESS");
    }
}
