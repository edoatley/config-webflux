package com.edoatley.person.routing;

import com.edoatley.person.handler.PersonHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PersonRouter {

    @Autowired
    PersonHandler personHandler;

    @Bean
    RouterFunction<?> routes() {
        return nest(path("/person").and(accept(MediaType.APPLICATION_JSON)),
                route(POST("/"), personHandler::createPerson)
                .andRoute(GET("/{id}"), personHandler::getById)
        );
    }

}
