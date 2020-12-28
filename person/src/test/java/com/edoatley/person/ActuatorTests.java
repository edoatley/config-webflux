package com.edoatley.person;

import com.edoatley.person.handler.PersonHandler;
import com.edoatley.person.repository.PersonRepository;
import com.mongodb.reactivestreams.client.MongoClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorTests {

    @MockBean
    PersonHandler personHandler;

    @MockBean
    PersonRepository personRepository;

    // required for health check
    @MockBean
    MongoClient client;

    @Autowired
    private WebTestClient webClient;

    @ParameterizedTest(name = "Testing actuator {arguments} returns ok")
    @ValueSource(strings = {
            "/actuator/info",
            "/actuator/health",
            "/actuator/env",
            "/actuator/beans"
    })
    void testActuatorSecurity(String path) {
        webClient.get().uri(path)
                .exchange()
                .expectStatus().isOk();
    }
}
