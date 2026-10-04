package com.iana.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IANA API - Organizador de Links & Livros")
                        .version("1.0.0")
                        .description("Documentação interativa das APIs do microserviço IANA para gerenciamento de links, livros e categorias.")
                        .contact(new Contact().name("Equipe IANA")));
    }
}
