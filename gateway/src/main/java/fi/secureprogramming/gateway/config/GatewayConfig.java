package fi.secureprogramming.gateway.config;

import fi.secureprogramming.gateway.dto.ProductDTO;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("create-product", r -> r.path("/products")
                        .and().method("POST")
                        .and().readBody(ProductDTO.class, productDTO -> true)
                        .uri("http://app:8080"))
                .route("get-products", r -> r.path("/products")
                        .and().method("GET")
                        .uri("http://app:8080"))
                .build();
    }
}
