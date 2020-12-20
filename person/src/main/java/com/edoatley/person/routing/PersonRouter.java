package com.edoatley.person.routing;

import com.edoatley.person.handler.PersonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class PersonRouter {

    @Bean
    RouterFunction<ServerResponse> peopleRoutes(PersonHandler personHandler) {
        return RouterFunctions.route()
            .GET("/people/{id}", personHandler::getPersonById)
            .GET("/people", personHandler::getAllPeople)
            .POST("/people", personHandler::createPerson)
            .GET("/ping", request -> ok().body(fromValue("pong")))
        .build();
    }

//    @Bean
//    RouterFunction<ServerResponse> peopleRoutes(PersonHandler personHandler) {
//        return RouterFunctions.route()
//            .GET("/people/{id}", RequestPredicates.contentType(MediaType.APPLICATION_JSON), personHandler::getPersonById)
//            .GET("/people", RequestPredicates.contentType(MediaType.APPLICATION_JSON), personHandler::getAllPeople)
//            .POST("/people", RequestPredicates.contentType(MediaType.APPLICATION_JSON), personHandler::createPerson)
//            .GET("/ping", RequestPredicates.contentType(TEXT_PLAIN), request -> ok().body(fromValue("pong")))
//        .build();
//    }
//        return RouterFunctions.route(GET("/ping")
//            .and(accept(TEXT_PLAIN)), request -> ok().body(fromValue("pong")));
}
