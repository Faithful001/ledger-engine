package com.king.ledgerengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ledgerEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ledger Engine API")
                        .description("Double-entry ledger engine with balanced-transaction validation and immutable audit trail")
                        .version("v1"));
    }
}