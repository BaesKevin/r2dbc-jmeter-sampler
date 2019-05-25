package be.kevinbaes.bap.jmetersampler.r2dbc;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import be.kevinbaes.bap.jmetersampler.domain.DeviceEvent;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcTestConfiguration.*;

public class R2dbcTest {

  private static final Logger LOG = LoggerFactory.getLogger(R2dbcTest.class);

  private final R2dbcTestConfiguration config;
  private ConnectionFactory connectionFactory;
  private final R2dbcRepository goalRepository;

  public R2dbcTest(R2dbcTestConfiguration config, ConnectionOptions connectionOptions) {
    this.config = config;
    R2dbcConnectionUtil connectionUtil = new R2dbcConnectionUtil(connectionOptions);
    this.connectionFactory = connectionUtil.postgresConnectionFactory();

    if(this.config.getDriverType().equals(POOLED)) {
      this.connectionFactory = connectionUtil.pooledConnectionFactory(connectionFactory);
    }

    this.goalRepository = new R2dbcRepository(connectionFactory);

    LOG.info("Initialized R2DBC test with config [{}]", config);
  }

  public List<DeviceEvent> performDatabaseQueries() {
    if(config.getQueryType().equals(INSERT)) {
      LOG.info("inserting [{}] times", config.getInsertCount());

      goalRepository.insertSequential(config.getInsertCount(), config.getRetryCount(), config.getRetryDelay()).block();
    } else if (config.getQueryType().equals(INSERT_INTERLEAVE)) {
      LOG.info("inserting [{}] times", config.getInsertCount());

      goalRepository.insertInterleaved(config.getInsertCount(), config.getRetryCount(), config.getRetryDelay()).block();
    } else {
      goalRepository
          .select()
          .collectList()
          .block();
    }

    return new ArrayList<>();
  }

  public void testEnded() {
    if(this.config.getDriverType().equals(POOLED)) {
      LOG.info("closing connection pool");
      ((ConnectionPool)this.connectionFactory).close();
    }
  }

}
