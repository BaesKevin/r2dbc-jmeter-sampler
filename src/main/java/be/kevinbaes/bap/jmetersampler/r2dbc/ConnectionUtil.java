package be.kevinbaes.bap.jmetersampler.r2dbc;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

import java.time.Duration;

public class ConnectionUtil {

  public static ConnectionPool pooledConnectionFactory(ConnectionFactory baseFactory) {
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(baseFactory)
        .validationQuery("SELECT 1")
        .maxIdleTime(Duration.ofMillis(1000))
        .maxSize(10)
        .build();

    return new ConnectionPool(configuration);
  }

  public static ConnectionFactory postgresConnectionFactory() {
    return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
        .host("127.0.0.1")
        .port(5432) // optional, defaults to 5432
        .username("postgres")
        .password("postgres")
        .database("postgres")
        .build());
  }

}
