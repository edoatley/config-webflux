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

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;


@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonIntegrationRealWebClientTest {

    private static final Logger log = LoggerFactory.getLogger(PersonIntegrationRealWebClientTest.class);
    private static final Person JUAN = new Person("qhfkuw3hefuejwf", "Juan");
    private static final Person DAVE = new Person("alorjfahbiae4rgh", "Dave");
    private static final Person LIZ = new Person("ahukfwlnxuhrej", "Liz");

    @LocalServerPort
    int port;

    private WebClient webClient;

    @BeforeEach
    void setupWebClient() {
        webClient = WebClient.create("http://localhost:"+ port);
    }

    @Test
    @DisplayName("Save and retrieve person by id")
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    void saveAPersonAndGetById() {
        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(JUAN))
                .retrieve()
                .onStatus(HttpStatus::isError, error -> fail("Failed posting person " + error.statusCode()))
                .bodyToMono(Person.class)
                .subscribe(p -> assertThat(p.getName()).isEqualTo("Juan"), error());

        webClient.get().uri("/people/qhfkuw3hefuejwf")
                .retrieve()
                .onStatus(HttpStatus::isError, error -> fail("Failed fetching person id qhfkuw3hefuejwf" + error.statusCode()))
                .bodyToMono(Person.class)
                .subscribe(checkPersonIs(JUAN));
    }

    @Test
    @DisplayName("Save and retrieve person by name")
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    void savePersonAndGetByName() {
        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(LIZ))
                .retrieve()
                .onStatus(HttpStatus::isError, error -> fail("Failed posting person " + error.statusCode()))
                .bodyToMono(Person.class)
                .subscribe(checkPersonIs(LIZ), error());

        webClient.get().uri(uriBuilder -> uriBuilder.path("/people").queryParam("name", "Liz").build())
                .retrieve()
                .onStatus(HttpStatus::isError, error -> fail("Failed fetching person named Liz" + error.statusCode()))
                .bodyToMono(Person.class)
                .subscribe(checkPersonIs(LIZ), error());
    }

    @Test
    @DisplayName("Save two people then retrieve them")
    @WithMockUser(username = "ed", roles = {"ADMIN"})
    void savePeopleAndGetAll() {
        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(LIZ))
                .retrieve()
                .onStatus(HttpStatus::isError, error -> fail("Failed posting Liz " + error.statusCode()))
                .bodyToMono(Person.class)
                .subscribe(checkPersonIs(LIZ), error());

        webClient.post().uri("/people").contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(DAVE))
                .retrieve()
                .onStatus(HttpStatus::isError, error -> fail("Failed posting Dave " + error.statusCode()))
                .bodyToMono(Person.class)
                .subscribe(checkPersonIs(DAVE), error());

        webClient.get().uri("/people")
                .retrieve()
                .onStatus(HttpStatus::isError, error -> fail("Failed fetching all people dave & Liz" + error.statusCode()))
                .bodyToFlux(Person.class)
                .collect(Collectors.toList())
                .subscribe(l -> assertThat(l).containsExactlyInAnyOrder(LIZ, DAVE), error());
    }

    private Consumer<Person> checkPersonIs(Person expectedPerson) {
        return p -> assertThat(p).isEqualTo(expectedPerson);
    }

    private Consumer<Throwable> error() {
        return t -> fail("Unexpected failure " + t.getMessage());
    }
}
