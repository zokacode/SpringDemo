package com.example;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EntityScan({"com.example"})
@EnableJpaAuditing
public class SpringDemo01Application extends SpringBootServletInitializer {

	@PostConstruct
	public void init() {
		// 強制設定預設時區
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
	}

	@Override
	public SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
		return applicationBuilder.sources(SpringDemo01Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringDemo01Application.class, args);
	}

}
