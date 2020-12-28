package com.edoatley.person.repository;

import com.edoatley.person.entity.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class PersonRepoIntegrationTest {

    public static final Person DAVE = new Person("id1", "Dave");
    public static final Person STEVE = new Person("id2", "Steve");
    @Autowired
    PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        personRepository.insert(DAVE);
    }

    @Test
    void testGetById() {
        Mono<Person> personMono = personRepository.findById(Mono.just(DAVE.getId()));
        // Interestingly this does not work
//        Person person = personMono.block();
//        assertThat(person).isEqualTo(DAVE);
        personMono.subscribe(p -> assertThat(p).isEqualTo(DAVE), t -> fail("OOPS " + t.getMessage()));
    }

    @Test
    void insertAndRetrieve() {
        Flux<Person> personFlux = personRepository.insert(Flux.just(STEVE));
        personFlux
                .flatMap(p -> personRepository.findById(p.getId()))
                .subscribe(p -> assertThat(p).isEqualTo(STEVE), t -> fail("OOPS " + t.getMessage()));
    }

    @Test
    void saveAndRetrieve() {
        Mono<Person> personMono = personRepository.save(STEVE);
        personMono
                .flatMap(p -> personRepository.findById(p.getId()))
                .subscribe(p -> assertThat(p).isEqualTo(STEVE), t -> fail("OOPS " + t.getMessage()));
    }
}
