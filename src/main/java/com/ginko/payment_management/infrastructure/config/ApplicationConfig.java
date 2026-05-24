package com.ginko.payment_management.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public OpenAPI paymentManagementOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Payment Management API")
				.version("v1")
				.description("API para gestion de proveedores y ordenes de pago."));
	}
}
