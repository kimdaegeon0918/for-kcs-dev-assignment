package com.example.demo.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "StockResponse")
public class StockResponse {
    private String companyName;  // 회사 이름
    private String tradeDate;    // 거래 날짜
    private long closingPrice;   // 종가
}
