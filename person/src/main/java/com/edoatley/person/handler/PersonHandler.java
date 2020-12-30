package com.edoatley.person.handler;

import com.edoatley.person.entity.Person;
import com.edoatley.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


    public Mono<ServerResponse> createPerson(ServerRequest request) {
        log.info("Creating a person");
        Mono<Person> personMono = request.bodyToMono(Person.class);
        return ServerResponse.status(HttpStatus.CREATED)
                    .body(personMono.flatMap(personRepository::save), Person.class)
//                .body(BodyInserters.fromPublisher(
//                        personMono.flatMap(personRepository::save), Person.class))
                .subscribeOn(APPLICATION_SCHEDULER);
    }

    // TODO: trying to make not found scenario work. Seem to need to access the people mono
    //       to allow switchIfEmpty to work
    public Mono<ServerResponse> getPersonById(ServerRequest request) {
        log.info("Finding a person by id");
        Mono<Person> person = personRepository.findById(request.pathVariable("id"));
        return person.flatMap(p -> ok().body(person, Person.class))
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
                .orElseThrow(() -> new IllegalArgumentException("No name provided to search for"));
        Flux<Person> people = personRepository.findByName(name);

        // can't use switchIfEmpty as the empty list is not considered empty
        return people.hasElements()
                .flatMap(found -> {
                    if (found) {
                        return ok().body(people, Person.class);
                    } else {
                        return notFound().build();
                    }
                }).subscribeOn(APPLICATION_SCHEDULER);
    }

    // TODO: can we write a function to make the found / not found logic read nicely and avoid duplication
//    <T> Mono<ServerResponse> returnListOrNotFound(Flux<T> result) {
//        return result.hasElements()
//                .flatMap(found -> {
//                    if (found) {
//                        return ok().body(result, Person.class);
//                    } else {
//                        return notFound().build();
//                    }
//                }).subscribeOn(APPLICATION_SCHEDULER);
//    }

}
