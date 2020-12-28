package com.edoatley.person;

import com.edoatley.person.entity.Person;
import com.edoatley.person.repository.PersonRepository;
import com.mongodb.reactivestreams.client.MongoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;


@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonIntegrationTest {

    public static final String AUTHENTICATION = "Authentication";
    private static final Logger log = LoggerFactory.getLogger(PersonIntegrationTest.class);

    @LocalServerPort
    int port;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MongoClient client;

    @Autowired
    PersonRepository personRepository;

    @Test
    @DisplayName("Save a person")
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    void saveAPerson() {
        log.info("Mon Client " + client.getClusterDescription().getShortDescription());

        personRepository.findAll().subscribe(p -> log.info(p.getName()));

        Person person = new Person("qhfkuw3hefuejwf", "Juan");
        webTestClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(person))
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class).getResponseBody().subscribe(r-> log.error(r));

        personRepository.findAll().subscribe(p -> log.info(p.getName()));
        personRepository.findById("qhfkuw3hefuejwf").subscribe(p -> assertThat(p.getName()).isEqualTo("Juan"), t -> fail("OOOPS " + t));
    }
}
