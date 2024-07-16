package com.example.demo.controller;

import com.example.demo.entity.Company;
import com.example.demo.entity.StocksHistory;
import com.example.demo.response.ApiResponse;
import com.example.demo.response.StockResponse;
import com.example.demo.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockHistoryController {

    @Value("${api.key}")
    private String API_KEY; // 환경 변수에서 API 키 값을 주입받습니다.

    private final StockHistoryService stockService; // StockHistoryService를 생성자 주입 방식으로 주입받습니다.

    /**
     * 단일 회사의 주식 히스토리를 조회합니다.
     *
     * @param companyCode 회사 코드
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param headerApiKey 헤더에서 제공된 API 키
     * @param paramApiKey 파라미터에서 제공된 API 키
     * @param format 응답 형식 (json 또는 xml)
     * @return 주식 히스토리 응답
     */
    @GetMapping(value = "/single")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getSingleStock(
            @RequestParam String companyCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "x-api-key", required = false) String headerApiKey,
            @RequestParam(value = "apikey", required = false) String paramApiKey,
            @RequestParam(value = "format", defaultValue = "json") String format) {

        return getStocks(Collections.singletonList(companyCode), startDate, endDate, headerApiKey, paramApiKey, format);
    }

    /**
     * 여러 회사의 주식 히스토리를 조회합니다.
     *
     * @param companyCode 회사 코드 목록
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param headerApiKey 헤더에서 제공된 API 키
     * @param paramApiKey 파라미터에서 제공된 API 키
     * @param format 응답 형식 (json 또는 xml)
     * @return 주식 히스토리 응답
     */
    @GetMapping(value = "/multiple")
    public ResponseEntity<ApiResponse<Map<String, List<StockResponse>>>> getMultipleStocks(
            @RequestParam List<String> companyCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "x-api-key", required = false) String headerApiKey,
            @RequestParam(value = "apikey", required = false) String paramApiKey,
            @RequestParam(value = "format", defaultValue = "json") String format) {

        return getStocks(companyCode, startDate, endDate, headerApiKey, paramApiKey, format);
    }

    /**
     * 주식 히스토리를 조회하는 내부 메서드.
     *
     * @param companyCodes 회사 코드 목록
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param headerApiKey 헤더에서 제공된 API 키
     * @param paramApiKey 파라미터에서 제공된 API 키
     * @param format 응답 형식 (json 또는 xml)
     * @param <T> 응답 데이터 유형
     * @return 주식 히스토리 응답
     */
    private <T> ResponseEntity<ApiResponse<T>> getStocks(
            List<String> companyCodes,
            LocalDate startDate,
            LocalDate endDate,
            String headerApiKey,
            String paramApiKey,
            String format) {

        // API 키가 제공되지 않은 경우
        if (headerApiKey == null && paramApiKey == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", "API Key is missing", null));
        }

        // API 키 설정
        String apiKey = headerApiKey != null ? headerApiKey : paramApiKey;

        // API 키가 유효하지 않은 경우
        if (!API_KEY.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("error", "Invalid API Key", null));
        }

        // 시작 날짜가 종료 날짜보다 늦은 경우
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", "startDate cannot be after endDate", null));
        }

        // 회사 코드별로 주식 히스토리를 조회하여 응답 데이터를 생성
        Map<String, List<StockResponse>> response = companyCodes.stream()
                .collect(Collectors.toMap(
                        companyCode -> companyCode,
                        companyCode -> {
                            Company company = stockService.getCompanyByCode(companyCode);
                            if (company == null) {
                                return null;
                            }
                            List<StocksHistory> stocksHistory = stockService.getStocksHistory(companyCode, startDate, endDate);
                            return stocksHistory.stream()
                                    .map(stock -> new StockResponse(company.getCompanyName(), stock.getTradeDate().toString(), stock.getClosePrice()))
                                    .collect(Collectors.toList());
                        }
                ));

        // 하나 이상의 회사 정보를 찾을 수 없는 경우
        if (response.values().contains(null)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("error", "One or more companies not found", null));
        }

        // 응답 형식에 따라 Content-Type 설정
        MediaType contentType = MediaType.APPLICATION_JSON;
        if ("xml".equalsIgnoreCase(format)) {
            contentType = MediaType.APPLICATION_XML;
        }

        // 성공적인 응답 반환
        return ResponseEntity.ok()
                .contentType(contentType)
                .body((ApiResponse<T>) new ApiResponse<>("success", "Data fetched successfully", response));
    }
}
