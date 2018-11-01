package com.example.reactivesql;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.net.URI;

@SpringBootApplication
public class ReactiveSqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSqlApplication.class, args);
	}
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}

@Configuration
class RepositoryConfiguration {

	@Bean
	CustomerRepository customerRepository(R2dbcRepositoryFactory factory) {
		return factory.getRepository(CustomerRepository.class);
	}

	@Bean
	R2dbcRepositoryFactory r2dbcRepositoryFactory(DatabaseClient client) {
		RelationalMappingContext relationalMappingContext = new RelationalMappingContext();
		relationalMappingContext.afterPropertiesSet();
		return new R2dbcRepositoryFactory(client, relationalMappingContext);
	}

	@Bean
	DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
		return DatabaseClient.builder().connectionFactory(connectionFactory).build();
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

	@Id
	private Integer id;
	private String email;

	Customer(String e) {
		this.email = e;
	}
}
/*
@Repository
class CustomerRepository {

	private final ConnectionFactory connectionFactory;

	CustomerRepository(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	Mono<Void> deleteById(Integer id) {
		return connection()
			.flatMapMany(cf -> cf.createStatement("delete from customer where id = $1")
				.bind("$1", id)
				.execute())
			.then();
	}

	Flux<Customer> save(Customer c) {
		Flux<? extends Result> many = connection()
			.flatMapMany(connection ->
				connection
					.createStatement("insert into customer( email) values ($1)")
					.bind("$1", c.getEmail())
					.add()
					.execute()
			);
		return many
			.switchMap(result -> Flux.just(new Customer(c.getId(), c.getEmail())));
	}

	Flux<Customer> findAll() {
		return connection()
			.flatMapMany(connection -> Flux.from(connection.createStatement("select * from customer").execute())
				.flatMap(r -> r.map((row, rowMetadata) -> new Customer(row.get("id", Integer.class), row.get("email", String.class)))));
	}

	private Mono<Connection> connection() {
		return Mono.from(this.connectionFactory.create());
	}

}


*/

@Configuration
class DataConfiguration {

	@Bean
	PostgresqlConnectionFactory postgresqlConnectionFactory(@Value("${spring.datasource.url}") String url) {
		URI uri = URI.create(url);
		String host = uri.getHost();
		String userInfo = uri.getUserInfo();
		String user = userInfo, pw = "";
		if (userInfo.contains(":")) {
			user = userInfo.split(":")[0];
			pw = userInfo.split(":")[1];
		}
		String path = uri.getPath().substring(1);
		PostgresqlConnectionConfiguration pgsql =
			PostgresqlConnectionConfiguration
				.builder()
				.database(path)
				.host(host)
				.password(pw)
				.username(user)
				.build();

		return new PostgresqlConnectionFactory(pgsql);
	}

}