package fi.secureprogramming.gateway.services;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * This class resolves the UUID of the mobile client making the request.
 * It implements the KeyResolver interface from Spring Cloud Gateway.
 */
@Component
@Primary
public class MobileClientResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Device-UUID"))
                .map(Mono::just)
                .orElse(Mono.empty());
    }
}
