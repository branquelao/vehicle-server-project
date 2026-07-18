package br.edu.unifaj.cc.poo.appcompraveiculoserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Server API")
                        .description("API para anúncios de veículos usados (carros e motos), " +
                                "consumida por um site e por um app Android.")
                        .version("v1"));
    }
}