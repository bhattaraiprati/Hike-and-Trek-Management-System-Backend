package com.example.treksathi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trek Sathi API")
                        .version("1.0")
                        .description("Hike and Trek Management System API")

                        .contact(new Contact()
                                .name("Trek Sathi Team")
                                .email("support@treksathi.com")))
                .servers(List.of(new Server().url("http://localhost:8080/api/v3/api-doc")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("User Apis"),
                        new Tag().name("Organizer Apis"),
                        new Tag().name("Chat Room APIS"),
                        new Tag().name("Event Apis"),
                        new Tag().name("Organizer Event Apis"),
                        new Tag().name("Event Registration Apis")
                        ));
    }
}
