package com.edoatley.person;

import com.edoatley.person.entity.Person;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.awt.print.Book;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonIntegrationTest {

    public static final String AUTHENTICATION = "Authentication";
    private static final Logger log = LoggerFactory.getLogger(PersonIntegrationTest.class);

    @LocalServerPort
    int port;

    @Value("${admin.user.name}")
    private String userName;
    @Value("${admin.user.password}")
    private String password;

    private WebClient webClient;

    @BeforeEach
    void initClient() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(logRequest());
                    exchangeFilterFunctions.add(logResponse());
                })
                .build();
    }

    @Test
    @DisplayName("Save a person and read it back")
    public void testSaveAndRetrieve() {

        // given
        Person person = webClient.post()
                .uri("/people")
//                .header(AUTHENTICATION, basicAuthHeader())
                .header("Authorization", "Basic " + Base64Utils
                        .encodeToString(("ed:password").getBytes(UTF_8)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new Person("3600DBE26F924A699D1850CE10A091F2", "Robert")), Person.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, ClientResponse::createException)
                .onStatus(HttpStatus::is5xxServerError, ClientResponse::createException)
                .bodyToMono(Person.class).block();
//
//        // when
//        Mono<Person> savedPersonMono = webClient.get().uri("/people/{id}", person.getId())
////                .header(AUTHENTICATION, this::basicAuthHeader)
//                .header("Authorization", "Basic " + Base64Utils
//                        .encodeToString(("ed:password").getBytes(UTF_8)))
//                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError, ClientResponse::createException)
//                .onStatus(HttpStatus::is5xxServerError, ClientResponse::createException)
//                .bodyToMono(Person.class);
//
//        // then
//        assertThat(savedPersonMono.block()).as("Posted Person should equal retrieved one").isEqualTo(personToSave);
    }

//    private String basicAuthHeader() {
//        return () -> "Basic " + Base64Utils.encodeToString(("ed:password").getBytes(UTF_8));
//        System.err.println(result);
//        String plainTextAuth = String.format("%s:%s", userName, password);
//        result = "Basic " + Base64Utils.encodeToString(plainTextAuth.getBytes(UTF_8));
//        System.err.println(result);
//        return result;
//    }

    ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Request: \n");
                sb.append("\n      url: " + clientRequest.url());
                sb.append("\n   method: " + clientRequest.method());
                sb.append("\n     body: " + clientRequest.body());
                sb.append("\n  headers: " + clientRequest.body());
                clientRequest
                        .headers()
                        .forEach((name, values) -> values.forEach(value -> sb.append("\n     " + name + ":" + value)));
                log.debug(sb.toString());
            }
            return Mono.just(clientRequest);
        });
    }
    ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Response: \n");
                sb.append("\n  status: " + clientResponse.statusCode());
                sb.append("\n    body: " + clientResponse.bodyToMono(Person.class).block());
                log.debug(sb.toString());
            }
            return Mono.just(clientResponse);
        });
    }
}
