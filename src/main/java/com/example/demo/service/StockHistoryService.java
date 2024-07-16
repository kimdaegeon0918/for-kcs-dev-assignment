package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.entity.StocksHistory;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.StocksHistoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockHistoryService {

    private final CompanyRepository companyRepository;
    private final StocksHistoryRepository stocksHistoryRepository;

    // 회사 코드를 통해 회사 정보를 조회하는 메서드
    public Company getCompanyByCode(String companyCode) {
        return companyRepository.findByCompanyCode(companyCode);
    }

    // 회사 코드와 날짜 범위를 통해 주식 히스토리를 조회하는 메서드
    public List<StocksHistory> getStocksHistory(String companyCode, LocalDate startDate, LocalDate endDate) {
        return stocksHistoryRepository.findByCompanyCodeAndTradeDateBetween(companyCode, startDate, endDate);
    }
}
