package com.example.demo.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor  // Lombok: 기본 생성자 추가
@AllArgsConstructor // Lombok: 모든 필드를 초기화하는 생성자 추가
@Table(name = "stocks_history")
public class StocksHistory {
    @Id
    private int id;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "close_price")
    private long closePrice;
}