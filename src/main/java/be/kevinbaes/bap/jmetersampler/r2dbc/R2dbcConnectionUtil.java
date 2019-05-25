package be.kevinbaes.bap.jmetersampler.r2dbc;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

import java.time.Duration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

public class R2dbcConnectionUtil {

  private final ConnectionOptions options;

  public R2dbcConnectionUtil(ConnectionOptions options) {
    this.options = options;
  }

  public ConnectionPool pooledConnectionFactory(ConnectionFactory baseFactory) {
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(baseFactory)
        .initialSize(10)
        .validationQuery("SELECT 1")
        .maxIdleTime(Duration.ofMillis(1000))
        .maxSize(10)
        .build();

    return new ConnectionPool(configuration);
  }

  public ConnectionFactory postgresConnectionFactory() {
//    return ConnectionFactories.get("r2dbc:postgresql://postgres:postgres@localhost:5432/postgres");

    return ConnectionFactories.get(ConnectionFactoryOptions.builder()
        .option(DRIVER, "postgresql")
        .option(HOST, options.getHost())
        .option(PORT, options.getPort())  // optional, defaults to 5432
        .option(USER, options.getUsername())
        .option(PASSWORD, options.getPassword())
        .option(DATABASE, options.getDatabase())
        .build());
  }

}
