package com.example.r2dbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@SpringBootTest
@RunWith(SpringRunner.class)
public class ReservationRepositoryTest {


	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	public void all() {

		Flux<Void> deleteAll = this.reservationRepository.findAll().flatMap(r -> this.reservationRepository.deleteById(r.getId()));
		StepVerifier
			.create(deleteAll)
			.expectNextCount(0)
			.verifyComplete();

		Flux<Reservation> reservationFlux = Flux.just("first", "second", "third")
			.map(name -> new Reservation(null, name))
			.flatMap(r -> this.reservationRepository.save(r));
		StepVerifier
			.create(reservationFlux)
			.expectNextCount(3)
			.verifyComplete();

		Flux<Reservation> all = this.reservationRepository.findAll();
		StepVerifier
			.create(all)
			.expectNextCount(3)
			.verifyComplete();

		Flux<Reservation> first = this.reservationRepository.findByName("first");
		StepVerifier
			.create( first)
			.expectNextCount(1)
			.verifyComplete();

	}


}