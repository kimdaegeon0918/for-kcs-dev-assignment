# 특정 기업의 주가(종가) 정보를 조회하여 응답을 주는 API 서버 개발

## 개발환경
- MacOS, Intellij

## 기술스택
- JAVA 17, SpringBoot 3.3.1

## 폴더구조

```java
src
├── main  
│   ├── java  
│   │   └── com
│   │       └── example
│   │           └── demo
│   │               ├── config  
│   │               │   ├── GlobalCorsConfig.java  // 글로벌 CORS 설정 클래스
│   │               │   └── RateLimiterConfig.java  // API Rate Limiter 설정 클래스
│   │               ├── controller  
│   │               │   └── StockHistoryController.java  // 주식 히스토리 관련 API 엔드포인트를 정의한 컨트롤러
│   │               ├── entity  
│   │               │   ├── Company.java  // 회사 엔티티 클래스
│   │               │   └── StocksHistory.java  // 주식 히스토리 엔티티 클래스
│   │               ├── error  
│   │               │   ├── CustomErrorResponse.java  // 사용자 정의 오류 응답 클래스
│   │               │   └── GlobalExceptionHandler.java  // 전역 예외 처리기 클래스
│   │               ├── interceptor  
│   │               │   └── RateLimiterInterceptor.java  // API Rate Limiter 인터셉터 클래스
│   │               ├── repository  
│   │               │   ├── CompanyRepository.java  // 회사 엔티티에 대한 작업을 처리하는 리포지토리
│   │               │   └── StocksHistoryRepository.java  // 주식 히스토리 엔티티에 대한 작업을 처리하는 리포지토리
│   │               ├── response  
│   │               │   ├── ApiResponse.java  // 표준 API 응답 형식을 정의한 클래스
│   │               │   └── StockResponse.java  // 주식 응답 데이터를 정의한 클래스
│   │               └── service  
│   │                   └── StockHistoryService.java  // 주식 히스토리 관련 비즈니스 로직을 처리하는 서비스 클래스
```

## API 명세서
- 단일 회사 주식 히스토리 조회
  - URL: /api/v1/stocks/single
  - Method: GET
  - Headers: x-api-key: {API_KEY}
  - Query Parameters:
    - companyCode (String, required): 조회할 회사 코드
    - startDate (String, required): 조회 시작 날짜 (YYYY-MM-DD 형식)
    - endDate (String, required): 조회 종료 날짜 (YYYY-MM-DD 형식)
    - apikey (String, optional): 쿼리 파라미터로 제공된 API 키
    - format (String, optional, default: "json"): 응답 형식 (json 또는 xml)

- 여러 회사 주식 히스토리 조회
  - URL: /api/v1/stocks/multiple
  - Method: GET
  - Headers: x-api-key: {API_KEY}
  - Query Parameters:
    - companyCode (String, required): 조회할 회사 코드 목록
    - companyCode (String, required): 조회할 회사 코드 목록
    - ... 조회할 회사들 각각 따로 파라미터에 추가
    - startDate (String, required): 조회 시작 날짜 (YYYY-MM-DD 형식)
    - endDate (String, required): 조회 종료 날짜 (YYYY-MM-DD 형식)
    - apikey (String, optional): 쿼리 파라미터로 제공된 API 키
    - format (String, optional, default: "json"): 응답 형식 (json 또는 xml)

### JPA 사용 이유

1. 확장성과 유지보수성의 이점을 얻을 수 있습니다.
2. 비즈니스 로직에 더 집중할 수 있습니다.

### 엔티티 생성

Company 엔티티

```java
package com.example.demo.entity;

import jakarta.persistence.*;

import lombok.Getter;

@Getter
@Entity
@Table(name = "company")
public class Company {
    @Id
    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "company_name")
    private String companyName;
}
```

StocksHistory 엔티티

```java
package com.example.demo.entity;

import jakarta.persistence.*;

import lombok.Getter;

import java.time.LocalDate;

@Entity
@Getter
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
```

## API 설계 및 설명

### RESTful 원칙을 준수하여 설계되었습니다. RESTful API는 자원을 URI로 식별하고, HTTP 메서드를 통해 자원에 대한 작업을 수행합니다. 이 설계에서는 주식 히스토리 데이터를 자원으로 간주하고, 다음과 같은 HTTP 메서드를 사용합니다:
- `GET`: 주식 히스토리 데이터를 조회합니다.

### URI에 버전 정보를 포함시켜 확장성을 고려했습니다. 이는 새로운 기능 추가나 API 변경 시 기존 클라이언트에 영향을 최소화합니다.
    
    `/api/v1/stocks`
    
    ```java
    @RequestMapping("/api/v1/stocks")
    public class StockController {
    ```

### 요청 및 응답 형식을 일관되게 유지하여 사용자가 예측 가능한 구조로 API를 사용할 수 있도록 했습니다. 모든 API 응답은 공통 포맷을 따르며, 상태 코드와 메시지를 명확히 포함합니다.
    - **ApiResponse 클래스**: 모든 응답을 표준화하기 위해 사용됩니다.
        - **속성**:
            - `status`: 응답 상태 (`success` 또는 `error`)
            - `message`: 응답 메시지
            - `data`: 실제 응답 데이터
        
        ```java
        package com.example.demo.response;
        
        import lombok.AllArgsConstructor;
        import lombok.Getter;
        import lombok.NoArgsConstructor;
        import lombok.Setter;
        
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public class ApiResponse<T> {
            private String status;  // 응답 상태
            private String message; // 응답 메시지
            private T data;         // 실제 응답 데이터
        }
        ```
![image](https://github.com/user-attachments/assets/99a83e47-3de1-4004-bb24-c8118316cec0)
![image](https://github.com/user-attachments/assets/336e8269-6bca-43c6-8dfa-057a9348301d)

### 전역 예외 처리기를 통해 모든 예외 상황에서 일관된 오류 응답을 제공하여, 클라이언트가 오류 원인을 쉽게 이해할 수 있도록 했습니다.
- **GlobalExceptionHandler 클래스**:
    - **역할**: 모든 예외를 처리하고 일관된 오류 메시지와 상태 코드를 반환합니다.
    - **이유**: 일관된 오류 응답을 통해 디버깅과 유지보수를 용이하게 합니다.

```java
package com.example.demo.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// @ControllerAdvice 어노테이션을 사용하여 전역 예외 처리기를 정의합니다.
// 이 클래스는 모든 컨트롤러에서 발생하는 예외를 처리합니다.
@ControllerAdvice
public class GlobalExceptionHandler {

    // MissingServletRequestParameterException 예외를 처리하는 메서드입니다.
    // 이 예외는 필수 요청 파라미터가 누락된 경우 발생합니다.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        // 누락된 파라미터 이름을 포함한 사용자 정의 오류 응답 객체를 생성합니다.
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "Missing required parameter: " + ex.getParameterName(),
                HttpStatus.BAD_REQUEST.value()
        );
        // 400 Bad Request 상태 코드와 함께 사용자 정의 오류 응답을 반환합니다.
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Exception 클래스를 상속받는 모든 예외를 처리하는 메서드입니다.
    // 이 메서드는 알 수 없는 예외가 발생했을 때 호출됩니다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGlobalException(Exception ex) {
        // 내부 서버 오류 메시지를 포함한 사용자 정의 오류 응답 객체를 생성합니다.
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        // 500 Internal Server Error 상태 코드와 함께 사용자 정의 오류 응답을 반환합니다.
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
### 필수 파라미터가 누락되었을 때 적절한 오류 메시지를 반환하여 API 사용자가 잘못된 요청을 쉽게 수정할 수 있도록 했습니다.
- **파라미터 검증 로직**:
    - 필수 파라미터 (`companyCode`, `startDate`, `endDate`)가 누락된 경우 `400 Bad Request` 응답을 반환합니다.
    - **이유**: 클라이언트가 API 사용 규칙을 명확히 이해할 수 있도록 돕습니다.
 
  ![image](https://github.com/user-attachments/assets/0535e3dc-5b25-4d4a-af41-67144ac4d874)

### CORS 설정을 통해 특정 도메인에서의 요청을 허용하여 보안과 유연성을 모두 고려했습니다.
- **GlobalCorsConfig 클래스**:
    - **역할**: 특정 도메인(`http://localhost:3000`)에서의 요청을 허용합니다.
    - **이유**: 클라이언트 애플리케이션이 API에 접근할 수 있도록 하면서도 보안을 유지합니다.

```java
allowed:
  origins: "http://localhost:3000,http://example.com" // 허용할 도메인을 추가
```

```java
package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig {
    @Value("${allowed.origins}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(parseAllowedOrigins(allowedOrigins))
                        .allowedMethods("GET") // 읽기만 허용
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // 허용도메인 파싱해서 리스트로 반환
    private String[] parseAllowedOrigins(String origins) {
        return origins.split(",");
    }
}
```

### 확장성과 재사용성

Lombok 라이브러리를 사용하여 보일러플레이트 코드를 줄이고, 유지보수성을 높였습니다. `@RequiredArgsConstructor`를 사용하여 의존성 주입을 간편하게 처리하였습니다. 또한, 엔티티, 서비스, 리포지토리 등 각 구성 요소는 역할에 따라 분리되어 있어 확장성과 재사용성이 높습니다.

### 보안
    
    API 키를 사용하여 API에 대한 접근을 제한합니다. 모든 요청은 유효한 API 키를 포함해야 하며, 유효하지 않은 경우 403 상태 코드를 반환합니다. API 키는 환경 변수로 관리하여 보안을 강화합니다.
![image](https://github.com/user-attachments/assets/9963cd89-6214-48fe-ae03-c3dfc1cd61d9)

민감한 정보를 dotenv라이브러리를 사용해 환경변수파일에서 불러와 보안을 강화합니다.

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

allowed:
  origins: "http://localhost:3000,http://example.com"

api:
  key: ${API_KEY}
```

### 구현한 API에 대해 단위 테스트와 통합 테스트를 구현

StockHistoryController 테스트

```java
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
```

CompanyRepositry 테스트

```java
package com.example.demo.repository;

import com.example.demo.entity.Company;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    public void testFindByCompanyCode() {
        // given
        Company company = new Company("AAPL", "Apple Inc.");
        companyRepository.save(company);

        // when
        Company foundCompany = companyRepository.findByCompanyCode("AAPL");

        // then
        assertThat(foundCompany).isNotNull();
        assertThat(foundCompany.getCompanyCode()).isEqualTo("AAPL");
        assertThat(foundCompany.getCompanyName()).isEqualTo("Apple Inc.");
    }
}
```

StocksHistoryRepository 테스트

```java
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
```

StockHistoryService 테스트

```java
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
```

통합테스트

```java
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
```

### request parameter(format=json, format=xml)로 요청받아 json과 xml 두가지 응답형식 제공 (기본값 json)
![image](https://github.com/user-attachments/assets/fe768b23-6182-485b-9f86-6d4d7c3b55a5)

### API Throttling
특정 API 키에 대한 호출을 10초에 10건으로 제한하는 Quota 기능을 구현했습니다. 
슬라이딩 윈도우 알고리즘을 사용했고 선택이유는 다음과 같습니다.
요청 수를 더 정밀하게 관리할 수 있습니다. 각 요청 시점의 시간을 저장하고, 해당 시간 창 내의 요청 수를 계산하기 때문에 메모리 사용이 효율적입니다. 비교적 간단하게 구현할 수 있으며, 기존의 API 요청 로직에 쉽게 통합될 수 있습니다.
![image](https://github.com/user-attachments/assets/0fe95a94-9413-4915-8f1f-e8ec45270861)

