package com.edoatley.person.routing;

import com.edoatley.person.entity.Person;
import com.edoatley.person.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.edoatley.person.util.TestData.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonRoutingTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    PersonRepository personRepository;

    @BeforeEach
    void mocks() {
        given(personRepository.findAll()).willReturn(Flux.just(ED, HANNAH, JON));

        given(personRepository.findById(ED.getId())).willReturn(Mono.just(ED));

        given(personRepository.insert(any(Publisher.class))).willReturn(Flux.just((JON)));
    }

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
    @WithMockUser(username = "bob", roles = {"BASIC"})
    @DisplayName("Fetch person by id")
    public void testGetById() {
        webClient.get().uri("/people/" + ED.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class)
                .isEqualTo(ED);
    }
    @Test
    @DisplayName("Fetch person by id requires BASIC")
    public void testGetByIdRequiresBasic() {
        webClient.get().uri("/people/" + ED.getId())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "bob", roles = {"BASIC"})
    @DisplayName("Get all people")
    void testGetAll() {
        webClient.get().uri("/people").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .contains(ED, HANNAH, JON);
    }
    @Test
    @DisplayName("Get all people requires BASIC")
    void testGetAllRequiresBasic() {
        webClient.get().uri("/people").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    @DisplayName("Add person as ADMIN")
    void testAddPerson() {
        Person actual = webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(JON))
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Person.class)
                .getResponseBody().blockFirst();

        assertThat(actual).isEqualTo(JON);
    }

    @Test
    @WithMockUser(username = "bob", roles = {"BASIC"})
    @DisplayName("Add person fails wthout ADMIN")
    void testAddRequiresAdmin() {
        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(JON))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Fail to add person anon")
    void testAddRequiresAuthentication() {
        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(JON))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Check that the user name password basic auth work")
    void checkBasicAuth() {
        webClient.get().uri("/people").accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + Base64Utils
                        .encodeToString(("ed:password").getBytes(UTF_8)))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Person.class)
                .contains(ED, HANNAH, JON);
    }
}