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
@JacksonXmlRootElement(localName = "ApiResponse")
public class ApiResponse<T> {
    private String status;  // 응답 상태
    private String message; // 응답 메시지
    private T data;         // 실제 응답 데이터
}
