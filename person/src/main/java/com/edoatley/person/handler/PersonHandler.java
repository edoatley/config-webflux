package com.edoatley.person.handler;

import com.edoatley.person.entity.Person;
import com.edoatley.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PersonHandler {

    private final PersonRepository personRepository;

    public Mono<ServerResponse> createPerson(ServerRequest request) {
        Mono<Person> personMono = request.bodyToMono(Person.class);
        return personMono.flatMap(student ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(personRepository.insert(personMono), Person.class));
    }

    public Mono<ServerResponse> getPersonById(ServerRequest request) {
        return ok().body(personRepository.findById(request.pathVariable("id")), Person.class);
    }

    public Mono<ServerResponse> getAllPeople(ServerRequest serverRequest) {
        return ok().body(personRepository.findAll(), Flux.class);
    }
}
