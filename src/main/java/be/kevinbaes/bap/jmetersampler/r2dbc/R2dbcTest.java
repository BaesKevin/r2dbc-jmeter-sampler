package be.kevinbaes.bap.jmetersampler.r2dbc;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcTestConfiguration.*;

public class R2dbcTest {

  private static final Logger LOG = LoggerFactory.getLogger(R2dbcTest.class);

  private final R2dbcTestConfiguration config;
  private ConnectionFactory connectionFactory;
  private final R2dbcGoalRepository goalRepository;

  public R2dbcTest(R2dbcTestConfiguration config) {
    this.config = config;
    this.connectionFactory = ConnectionUtil.postgresConnectionFactory();

    if(this.config.getDriverType().equals(POOLED)) {
      this.connectionFactory = ConnectionUtil.pooledConnectionFactory(connectionFactory);
    }

    this.goalRepository = new R2dbcGoalRepository(connectionFactory);

    LOG.info("Initialized R2DBC test with config [{}]", config);
  }

  public void performDatabaseQueries() {
    if(config.getQueryType().equals(INSERT)) {
      LOG.info("inserting [{}] times", config.getInsertCount());

      goalRepository.insertSingleConnection(config.getInsertCount()).block();
    } else if (config.getQueryType().equals(INSERT_INTERLEAVE)) {
      LOG.info("inserting [{}] times", config.getInsertCount());

      goalRepository.insertInterleaved(config.getInsertCount()).block();
    } else {
      goalRepository
          .select()
          .block();
    }
  }

  public void testEnded() {
    if(this.config.getDriverType().equals(POOLED)) {
      ((ConnectionPool)this.connectionFactory).close();
    }
  }

}
