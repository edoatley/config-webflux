package com.edoatley.person;

import com.edoatley.person.entity.Person;
import com.edoatley.person.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;


@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonIntegrationRealWebClientTest {

    public static final String AUTHENTICATION = "Authentication";
    private static final Logger log = LoggerFactory.getLogger(PersonIntegrationRealWebClientTest.class);
    private static final Person JUAN = new Person("qhfkuw3hefuejwf", "Juan");

    @LocalServerPort
    int port;

    private WebClient webClient;

    @Autowired
    PersonRepository personRepository;


    @BeforeEach
    void setupWebClient() {
        webClient = WebClient.create("http://localhost:"+ port);
    }

    @Test
    @DisplayName("Save and retrieve person")
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    void saveAPerson() {
        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(JUAN))
                .retrieve()
                .bodyToMono(Person.class)
                .subscribe(p -> assertThat(p.getName()).isEqualTo("Juan"), t -> fail("OOOPS " + t.getMessage()));

        webClient.get().uri("/people/qhfkuw3hefuejwf")
                .retrieve()
                .bodyToMono(Person.class)
                .subscribe(p -> assertThat(p).isEqualTo(JUAN));
    }
    @Test
    @DisplayName("Save and retrieve person returning result")
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    void saveAPerson2() {
        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(JUAN))
                .retrieve()
                .bodyToMono(Person.class)
                .subscribe(p ->  assertThat(p).isEqualTo(JUAN), t -> fail("OOOPS " + t.getMessage()));

        webClient.get().uri("/people/qhfkuw3hefuejwf")
                .retrieve()
                .bodyToMono(Person.class)
                .subscribe(p -> assertThat(p).isEqualTo(JUAN));
    }
}
