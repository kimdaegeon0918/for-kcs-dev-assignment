package com.example.demo.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private static final long QUOTA_LIMIT = 10; // 최대 요청 수
    private static final long TIME_WINDOW = TimeUnit.SECONDS.toMillis(10); // 시간 창 (10초)

    // API 키별로 요청 시간 기록을 저장하는 맵
    private Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("x-api-key");
        // API Key가 없는 경우를 무시하고, 컨트롤러에서 처리하도록 넘깁니다.
        if (apiKey == null) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        requestTimestamps.putIfAbsent(apiKey, new LinkedList<>());

        Deque<Long> timestamps = requestTimestamps.get(apiKey);

        synchronized (timestamps) {
            // 현재 시간보다 TIME_WINDOW 이전의 모든 타임스탬프를 제거합니다.
            while (!timestamps.isEmpty() && currentTime - timestamps.peekFirst() > TIME_WINDOW) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < QUOTA_LIMIT) {
                timestamps.addLast(currentTime);
                return true;
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429 상태 코드 설정
                response.getWriter().write("Quota exceeded. Try again later.");
                return false;
            }
        }
    }
}
