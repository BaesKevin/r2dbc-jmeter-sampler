package be.kevinbaes.bap;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

import java.time.Duration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

public class R2dbcConfig {

  public ConnectionPool pooledConnectionFactory() {
    ConnectionFactory connectionFactory = postgresConnectionFactory();

    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .validationQuery("SELECT 1")
        .maxIdleTime(Duration.ofMillis(1000))
        .maxSize(20)
        .build();

    return new ConnectionPool(configuration);
  }

  public ConnectionFactory postgresConnectionFactory() {
    return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
        .host("127.0.0.1")
        .port(5432) // optional, defaults to 5432
        .username("postgres")
        .password("postgres")
        .database("postgres")
        .build());
  }

}
