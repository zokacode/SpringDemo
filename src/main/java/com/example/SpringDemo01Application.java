package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EntityScan({"com.example"})
@EnableJpaAuditing
public class SpringDemo01Application extends SpringBootServletInitializer {

	@Override
	public SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
		return applicationBuilder.sources(SpringDemo01Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringDemo01Application.class, args);
	}

}
