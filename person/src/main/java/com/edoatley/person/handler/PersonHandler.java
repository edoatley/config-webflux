package com.edoatley.person.handler;

import com.edoatley.person.entity.Person;
import com.edoatley.person.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class PersonHandler {

    @Autowired
    PersonRepository personRepository;

    public Mono<ServerResponse> createPerson(ServerRequest request) {
        personRepository.insert(request.bodyToMono(Person.class)).subscribe();
        return ok().build();
    }


    public Mono<ServerResponse> getById(ServerRequest request) {
        return ok().body(personRepository.findById(request.pathVariable("id")), Person.class);
    }
}
