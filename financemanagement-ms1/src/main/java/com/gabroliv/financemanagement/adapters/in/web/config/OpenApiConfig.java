package com.gabroliv.financemanagement.adapters.in.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI financialManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Financial Management API - MS1 Users")
                        .description("API responsável pelo gerenciamento de usuários do sistema financeiro.")
                        .version("v1")
                        .license(new License()
                                .name("Beca Java - NTT DATA")));
    }
}
