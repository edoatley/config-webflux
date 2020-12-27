package com.edoatley.person.routing;

import com.edoatley.person.handler.PersonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class PersonRouter {

    @Bean
    RouterFunction<ServerResponse> peopleRoutes(PersonHandler personHandler) {
        return RouterFunctions.route()
            .GET("/people/{id}", personHandler::getPersonById)
            .GET("/people", request -> request.queryParams().containsKey("name"), personHandler::peopleByName)
            .GET("/people", personHandler::getAllPeople)
            .POST("/people", personHandler::createPerson)
            .GET("/ping", request -> ok().body(fromValue("pong")))
        .build();
    }
}
