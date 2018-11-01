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
public class CustomerRepositoryTest {

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	public void all() throws Exception {

		Flux<Void> deleteAll = this.customerRepository
			.findAll()
			.flatMap(customer -> this.customerRepository.deleteById(customer.getId()));

		StepVerifier
			.create(deleteAll)
			.expectNextCount(0)
			.verifyComplete();

		Flux<Customer> insertRecords = Flux
			.just("first@email.com", "second@email.com", "third@email.com")
			.map(email -> new Customer(null, email))
			.flatMap(customer -> this.customerRepository.save(customer));

		StepVerifier
			.create(insertRecords)
			.expectNextCount(3)
			.verifyComplete();

		Flux<Customer> all = this.customerRepository.findAll();
		StepVerifier
			.create(all)
			.expectNextCount(3)
			.verifyComplete();

	}

}