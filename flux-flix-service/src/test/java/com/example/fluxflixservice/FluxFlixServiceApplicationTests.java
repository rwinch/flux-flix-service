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

@RunWith(SpringRunner.class)
@SpringBootTest
public class FluxFlixServiceApplicationTests {

    @Autowired
    private MovieRepository movies;

    int SIZE = 2;

    @Test
    public void getEventsTakeWithNoVirtualTimeWorks() {
        StepVerifier.create(movies
                .findAll()
                .take(1)
                .map( movie -> movie.getId())
                .flatMap(this::eventsById)
                .take(SIZE)
                .collectList()
            )
            .thenAwait(Duration.ofHours(1))
            .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
            .verifyComplete();
    }

    @Test
    public void getEventsTakeWithVirtualTimeHoursHangs() {
        StepVerifier.withVirtualTime(() -> movies
                .findAll()
                .take(1)
                .map( movie -> movie.getId())
                .flatMap(this::eventsById)
                .take(SIZE)
                .collectList()
            )
            .thenAwait(Duration.ofHours(1))
            .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
            .verifyComplete();
    }

    //

    @Test
    public void getEventsByMovieTakeWithVirtualTimeAndEventsWithSpringDataHangs() {
        StepVerifier.withVirtualTime(() -> this.movies.findAll()
                .take(1)
                .flatMap(movie -> {
                    Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
                    return interval.map( tick -> new MovieEvent(new Date(), movie));
                })
                .take(SIZE)
                .collectList()
            )
            .thenAwait(Duration.ofHours(1))
            .consumeNextWith(list -> Assert.assertTrue(list.size() == SIZE))
            .verifyComplete();
    }

    @Test
    public void getEventsByMovieTakeWithVirtualTimeAndEventsWithNoSpringDataWorks() {
        StepVerifier.withVirtualTime(() -> Flux.just(new Movie("Mocked"))
                .take(1)
                .flatMap(movie -> {
                    Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
                    return interval.map( tick -> new MovieEvent(new Date(), movie));
                })
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

    Flux<MovieEvent> eventsById(String id) {
        return this.movies.findById(id).flatMapMany(movie -> {
            Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

            return interval.map( tick -> new MovieEvent(new Date(), movie));
        });
    }
}
