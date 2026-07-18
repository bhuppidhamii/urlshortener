package com.bhuppi.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("URL Shortener API")
                                .version("1.0.0")
                                .description(
                                        "A production-ready URL shortening service with analytics, pagination, sorting and dynamic filtering.")
                                .contact(
                                        new Contact()
                                                .name("Bhupendra Singh Dhami")
                                                .email("dhamibhupendra3@gmail.com")
                                                .url("https://github.com/bhuppidhamii/urlshortener/tree/develop")

                                ));
    }
}