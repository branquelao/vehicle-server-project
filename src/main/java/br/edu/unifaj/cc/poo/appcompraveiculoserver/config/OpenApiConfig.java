package br.edu.unifaj.cc.poo.appcompraveiculoserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Server API")
                        .description("API para anúncios de veículos usados (carros e motos).")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("Autenticação").description("Login e emissão de token JWT"),
                        new Tag().name("Usuários").description("Cadastro, consulta e gestão de contas de usuário (compradores e vendedores)"),
                        new Tag().name("Veículos").description("Anúncios de veículos (carros e motos): busca, cadastro e gestão"),
                        new Tag().name("Opcionais").description("Catálogo de opcionais disponíveis para associar a um veículo"),
                        new Tag().name("Uploads").description("Envio e consulta de imagens usadas em anúncios de veículos e perfis"),
                        new Tag().name("Favoritos").description("Gestão de veículos favoritados pelo usuário autenticado"),
                        new Tag().name("Mensagens").description("Conversas entre comprador e vendedor sobre um anúncio de veículo"),
                        new Tag().name("Avaliações").description("Reputação de vendedores, avaliada por compradores com quem já conversaram"),
                        new Tag().name("Buscas salvas").description("Filtros de busca salvos pelo usuário, com alertas automáticos de novos anúncios compatíveis")
                ));
    }
}