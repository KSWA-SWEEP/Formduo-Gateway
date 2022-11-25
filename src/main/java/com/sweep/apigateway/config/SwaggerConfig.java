package com.sweep.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Bean
    public CommandLineRunner openApiGroups(
            RouteDefinitionLocator locator,
            SwaggerUiConfigParameters swaggerUiParameters) {
        return args -> Objects.requireNonNull(locator
                        .getRouteDefinitions().collectList().block())
                .stream()
//                .filter(rou)
                .map(RouteDefinition::getId)
                .filter(id -> id.matches(".*-service"))
                .map(id -> id.replace("-service", ""))
                .forEach(swaggerUiParameters::addGroup);
    }

//    private final RouteDefinitionLocator locator;
//
//    @Bean
//    public List<GroupedOpenApi> apis() {
//        List<GroupedOpenApi> groups = new ArrayList<>();
//        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
//        definitions.stream().filter(routeDefinition -> routeDefinition.getId().matches(".*_route")).forEach(routeDefinition -> {
//            String name = routeDefinition.getId().replaceAll("_route", "");
//            GroupedOpenApi api = GroupedOpenApi.builder().pathsToMatch("/" + name + "/**").setGroup(name).build();
//            groups.add(api);
//        });
//        return groups;
//    }
}