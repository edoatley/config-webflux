package com.edoatley.person.handler;

import com.edoatley.person.entity.Person;
import com.edoatley.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PersonHandler {

    private static final Logger log = LoggerFactory.getLogger(PersonHandler.class);
    public static final Scheduler APPLICATION_SCHEDULER = Schedulers.boundedElastic();

    private final PersonRepository personRepository;


    // TODO: trying to make not found scenario work. Not sure why fromPublisher seems to be required...
    public Mono<ServerResponse> createPerson(ServerRequest request) {
        log.info("Creating a person");
        Mono<Person> personMono = request.bodyToMono(Person.class);
        return ServerResponse.status(HttpStatus.CREATED)
                .body(BodyInserters.fromPublisher(
                        personMono.flatMap(personRepository::save), Person.class))
                .subscribeOn(APPLICATION_SCHEDULER);
    }

    public Mono<ServerResponse> getPersonById(ServerRequest request) {
        log.info("Finding a person by id");
        return personRepository.findById(request.pathVariable("id"))
                .flatMap(person -> ok().body(person, Person.class))
                .switchIfEmpty(notFound().build())
                .subscribeOn(APPLICATION_SCHEDULER);
    }

    public Mono<ServerResponse> getAllPeople(ServerRequest serverRequest) {
        log.info("Finding all people");
        return ok()
                .body(BodyInserters.fromPublisher(
                        personRepository.findAll(), Person.class))
                .subscribeOn(APPLICATION_SCHEDULER);
    }

    public Mono<ServerResponse> peopleByName(ServerRequest serverRequest) {
        log.debug("Finding person by name");
        String name = serverRequest.queryParam("name")
                .orElseThrow(()->  new IllegalArgumentException("No name provided to search for"));
        return ok()
                .body(personRepository.findByName(name), Person.class)
                .subscribeOn(APPLICATION_SCHEDULER);
    }
}
