package com.example.reactivesql;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

//@EnableR2dbcRepositories
@SpringBootApplication
public class ReactiveSqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSqlApplication.class, args);
	}
}
//
//interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
//}


@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {

	@Id
	private Integer id;
	private String name;

	Reservation(String e) {
		this.name = e;
	}
}

@Repository
class ReservationRepository {

	private final ConnectionFactory connectionFactory;

	ReservationRepository(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	Mono<Void> deleteById(Integer id) {
		return connection()
			.flatMapMany(cf -> cf.createStatement("delete from reservation where id = $1")
				.bind("$1", id)
				.execute())
			.then();
	}

	Flux<Reservation> save(Reservation c) {
		Flux<? extends Result> many = connection()
			.flatMapMany(connection ->
				connection
					.createStatement("insert into reservation( name ) values ($1)")
					.bind("$1", c.getName())
					.add()
					.execute()
			);
		return many
			.switchMap(result -> Flux.just(new Reservation(c.getId(), c.getName())));
	}

	Flux<Reservation> findAll() {
		return connection()
			.flatMapMany(connection -> Flux.from(connection.createStatement("select * from reservation").execute())
				.flatMap(r -> r.map((row, rowMetadata) -> new Reservation(row.get("id", Integer.class), row.get("name", String.class)))));
	}

	private Mono<Connection> connection() {
		System.err.println("CONNECTION()");
		return Mono.from(this.connectionFactory.create());
	}

}


@Configuration
class ConnectionFactoryConfiguration {

	@Bean
	ConnectionFactory connectionFactory(@Value("${spring.datasource.url}") String url) {
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

@Configuration
class DataConfiguration extends AbstractR2dbcConfiguration {

	private final ConnectionFactory connectionFactory;

	DataConfiguration(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@Override
	public ConnectionFactory connectionFactory() {
		return this.connectionFactory;
	}
}