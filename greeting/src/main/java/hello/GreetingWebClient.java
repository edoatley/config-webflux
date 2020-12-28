package hello;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class GreetingWebClient<result> {
    private WebClient client = WebClient.create("http://localhost:8080");

    private WebClient.ResponseSpec result = client.get()
            .uri("/hello")
            .accept(MediaType.TEXT_PLAIN)
            .retrieve(); // swapped retrieve for deprecated exchange()
//            .exchange();

    public String getResult() {
        return ">>> result = " + result.bodyToMono(String.class).block();
//        return ">> result = " + result.flatMap(res -> res.bodyToMono(String.class)).block();
    }
}
