package com.example.demo.repository;

import com.example.demo.entity.StocksHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StocksHistoryRepository extends JpaRepository<StocksHistory, Integer> {
    List<StocksHistory> findByCompanyCodeAndTradeDateBetween(String companyCode, LocalDate startDate, LocalDate endDate);
}
