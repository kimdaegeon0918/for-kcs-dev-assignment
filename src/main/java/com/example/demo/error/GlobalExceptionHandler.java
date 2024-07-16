package com.example.demo.error;

import com.example.demo.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> handleMissingParams(MissingServletRequestParameterException ex) {
        // 누락된 파라미터 이름을 포함한 사용자 정의 오류 응답 객체를 생성합니다.
        ApiResponse<String> errorResponse = new ApiResponse<>(
                "error",
                "Missing required parameter: " + ex.getParameterName(),
                null
        );
        // 400 Bad Request 상태 코드와 함께 사용자 정의 오류 응답을 반환합니다.
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Exception 클래스를 상속받는 모든 예외를 처리하는 메서드입니다.
    // 이 메서드는 알 수 없는 예외가 발생했을 때 호출됩니다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        // 내부 서버 오류 메시지를 포함한 사용자 정의 오류 응답 객체를 생성합니다.
        ApiResponse<String> errorResponse = new ApiResponse<>(
                "error",
                "Internal Server Error",
                null
        );
        // 500 Internal Server Error 상태 코드와 함께 사용자 정의 오류 응답을 반환합니다.
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
