package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public MapPropertySource dotenvConfig(ConfigurableEnvironment environment) {
		Dotenv dotenv = Dotenv.load();
		Map<String, Object> envMap = new HashMap<>();
		dotenv.entries().forEach(entry -> envMap.put(entry.getKey(), entry.getValue()));
		MapPropertySource propertySource = new MapPropertySource("dotenv", envMap);
		environment.getPropertySources().addLast(propertySource);

		// 환경 변수 로드 확인을 위한 로그 추가
		System.out.println("Environment variables loaded:");
		envMap.forEach((key, value) -> System.out.println(key + ": " + value));

		return propertySource;
	}
}
