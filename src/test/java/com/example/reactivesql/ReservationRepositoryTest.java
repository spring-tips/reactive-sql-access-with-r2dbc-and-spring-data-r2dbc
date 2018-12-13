package com.example.reactivesql;

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
	public void all() throws Exception {

		Flux<Void> deleteAll = this.reservationRepository
			.findAll()
			.flatMap(reservation -> this.reservationRepository.deleteById(reservation.getId()));

		StepVerifier
			.create(deleteAll)
			.expectNextCount(0)
			.verifyComplete();

		Flux<Reservation> insertRecords = Flux
			.just("first@email.com", "second@email.com", "third@email.com")
			.map(email -> new Reservation(null, email))
			.flatMap(reservation -> this.reservationRepository.save(reservation));

		StepVerifier
			.create(insertRecords)
			.expectNextCount(3)
			.verifyComplete();

		Flux<Reservation> all = this.reservationRepository.findAll();
		StepVerifier
			.create(all)
			.expectNextCount(3)
			.verifyComplete();

	}

}