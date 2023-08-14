package com.arcadag.productcompositeservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI getOpenApiDocumentation() {

        return new OpenAPI()
                .info(new Info().title("Sample API")
                        .description("Description of the API...")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Name of Contact")
                                .url("url to contact")
                                .email("arcadag@mail.com"))
                        .termsOfService("My Terms of service")
                        .license(new License()
                                .name("My license")
                                .url("My license url")))
                .externalDocs(new ExternalDocumentation()
                        .description("My wiki page")
                        .url("My wiki url"));


    }
}
