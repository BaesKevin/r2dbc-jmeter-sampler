package be.kevinbaes.bap.jmetersampler.r2dbc;

import be.kevinbaes.bap.jmetersampler.Repository;
import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcTestConfiguration.*;

public class R2dbcTest {

  private static final Logger LOG = LoggerFactory.getLogger(R2dbcTest.class);

  private final R2dbcTestConfiguration config;
  private ConnectionFactory connectionFactory;
  private final Repository<?> repository;

  public R2dbcTest(R2dbcTestConfiguration config, ConnectionOptions connectionOptions) {
    this.config = config;
    LOG.info("initialize R2DBC test with connection options: [{}]", connectionOptions);
    R2dbcConnectionUtil connectionUtil = new R2dbcConnectionUtil(connectionOptions);
    this.connectionFactory = connectionUtil.postgresConnectionFactory();

    if(this.config.getDriverType().equals(POOLED)) {
      this.connectionFactory = connectionUtil.pooledConnectionFactory(connectionFactory);
    }

    this.repository = new R2dbcRepository(connectionFactory, config);

    LOG.info("Initialized R2DBC test with config [{}]", config);
  }

  public List<?> performDatabaseQueries(SampleResult result) {
    if(config.getQueryType().equals(INSERT)) {
      LOG.info("inserting [{}] times sequential", config.getInsertCount());

      repository.insertSequential(result);
    } else if (config.getQueryType().equals(INSERT_INTERLEAVE)) {
      LOG.info("inserting [{}] times interleaved", config.getInsertCount());

      repository.insertInterleaved(result);
    } else {
      repository.select(result);
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
