package com.example.fluxflixservice;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FluxFlixServiceApplicationTests {

    @Autowired
    private MovieService service;

    int SIZE = 2;

    @Test
    public void getEventsTakeWithNoVirtualTimeWorks() {
        StepVerifier.create(service
                .all()
                .take(1)
                .map( movie -> movie.getId())
                .flatMap(service::events)
                .take(SIZE)
                .collectList()
            )
            .thenAwait(Duration.ofHours(1))
            .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
            .verifyComplete();
    }

    @Test
    public void getEventsTakeWithVirtualTimeHoursHangs() {
        StepVerifier.withVirtualTime(() -> service
                .all()
                .take(1)
                .map( movie -> movie.getId())
                .flatMap(service::events)
                .take(SIZE)
                .collectList()
            )
            .thenAwait(Duration.ofHours(1))
            .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
            .verifyComplete();
    }

    @Test
    public void gh776() {
        StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofMillis(1))
                .map(tick -> new Date())
                .take(SIZE)
                .collectList()
            )
            .thenAwait(Duration.ofHours(1000))
            .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
            .verifyComplete();
    }
}
