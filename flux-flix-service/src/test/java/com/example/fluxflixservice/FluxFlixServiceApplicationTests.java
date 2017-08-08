package com.example.fluxflixservice;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FluxFlixServiceApplicationTests {

    @Autowired
    private MovieService service;

    int SIZE = 2;

    @Test
    public void getEventsTakeWithNoVirtualTimeWorks() {
        Movie movie = service.all().blockFirst();

        StepVerifier.create(service.events(movie.getId()).take(SIZE).collectList())
                .thenAwait(Duration.ofHours(1))
                .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
                .verifyComplete();
    }

    @Test
    public void getEventsTakeWithVirtualTimeHoursHangs() {
        Movie movie = service.all().blockFirst();

        StepVerifier.withVirtualTime(() -> service.events(movie.getId()).take(SIZE).collectList())
                .thenAwait(Duration.ofHours(1))
                .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
                .verifyComplete();
    }
}
