package com.example.demo;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

@RequestMapping("/test")
@RestController()
class Ctrl {
    AtomicInteger c = new AtomicInteger();

    private static final Logger log = LoggerFactory.getLogger(Ctrl.class);

    private WebClient wc;

    public Ctrl(WebClient.Builder wcb, @Value("${server.port:8080}") int port) {
        this.wc = wcb.baseUrl("http://localhost:" + port).build();
    }

    @GetMapping
    public Mono<String> getRequestTimeout() {
        return wc.get()
                .uri("/test/delay/" + c.getAndIncrement())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(1), Mono.just("timeOuted"))
                ;
    }

    @GetMapping("/delay/{id}")
    public Mono<?> delay(@PathVariable String id) {
        return Mono.delay(Duration.ofMillis(200))
                .doOnNext(aLong -> log.info("complete delay id: {}", id));
    }

}
