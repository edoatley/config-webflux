package com.edoatley.person.routing;

import com.edoatley.person.config.WebSecurityConfig;
import com.edoatley.person.entity.Person;
import com.edoatley.person.handler.PersonHandler;
import com.edoatley.person.repository.PersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.edoatley.person.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@ContextConfiguration(classes = { WebSecurityConfig.class, PersonRouter.class, PersonHandler.class})
class PersonRoutingTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    PersonRepository personRepository;

    @Test
    @DisplayName("Check the /ping endpoint responds ok")
    void testPing() {
        webClient.get().uri("/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("pong");
    }

    @Test
    @DisplayName("Fetch person by id")
    public void testGetById() {
        String id = ED.getId();
        given(personRepository.findById(id)).willReturn(Mono.just(ED));

        webClient.get().uri("/people/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .isEqualTo(ED);
    }

    @Test
    @DisplayName("Get all people")
    void testGetAll() {
        Flux<Person> people = Flux.just(ED, HANNAH, JON);
        given(personRepository.findAll()).willReturn(people);

        webClient.get().uri("/people").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .contains(ED, HANNAH, JON);
    }

    @Test
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    @DisplayName("Add person")
    void testAddPerson() {
        given(personRepository.insert(any(Publisher.class))).willReturn(Flux.just((JON)));

        Person actual = webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(JON))
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Person.class)
                .getResponseBody().blockFirst();

        assertThat(actual).isEqualTo(JON);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Fail to add person anon")
    void testRequireAuthentication() {
        given(personRepository.insert(any(Publisher.class))).willReturn(Flux.just((JON)));

        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(JON))
                .exchange()
                .expectStatus().isForbidden();
    }

}