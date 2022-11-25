package com.sweep.apigateway.filter;

import com.sweep.apigateway.jwt.TokenProvider;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationGatewayFilter extends
        AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilter.Config> {

//    private static final String ROLE_KEY = "role";

    private final TokenProvider tokenProvider;
    public static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationGatewayFilter(TokenProvider tokenProvider) {
        super(Config.class);
        this.tokenProvider = tokenProvider;
    }

//    @Override
//    public List<String> shortcutFieldOrder() {
//        return Collections.singletonList(ROLE_KEY);
//    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if (!containsAuthorization(request)) {
                return onError(response, "missing authorization header", HttpStatus.BAD_REQUEST);
            }

            String token = extractToken(request);
            if(!StringUtils.hasText(token)){
                return onError(response, "{\"error\": \"EMPTY_TOKEN\", \"message\" : \"토큰 값이 비어있습니다.\"}", HttpStatus.BAD_REQUEST);
            }

            int flag = tokenProvider.validateToken(token);

            if (flag == 1){ // 유효한 토큰
                String email = tokenProvider.getMemberEmailByToken(token);
                addAuthorizationHeaders(request, email);
                return chain.filter(exchange);

            } else if (flag == 2) { // 만료된 토큰
                return onError(response, "{\"error\": \"ACCESS_TOKEN_EXPIRED\", \"message\" : \"엑세스토큰이 만료되었습니다.\"}", HttpStatus.BAD_REQUEST);
            } else { // 잘못된 토큰
                return onError(response, "{\"error\": \"BAD_TOKEN\", \"message\" : \"잘못된 토큰 값입니다.\"}", HttpStatus.BAD_REQUEST);
            }
        };
    }

    private boolean containsAuthorization(ServerHttpRequest request) {
        return request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }

//    private boolean hasRole(TokenUser tokenUser, String role) {
//        return role.equals(tokenUser.getRole());
//    }

    private void addAuthorizationHeaders(ServerHttpRequest request, String email) {
        request.mutate()
                .header("X-Authorization-Id", email)
//                .header("X-Authorization-Role", tokenUser.getRole())
                .build();
    }

    private Mono<Void> onError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        DataBuffer buffer = response.bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

//    @Setter
    public static class Config {
//        private String role;

    }

}