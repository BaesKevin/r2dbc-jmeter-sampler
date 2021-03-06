package be.kevinbaes.bap.jmetersampler.jdbc;

import be.kevinbaes.bap.jmetersampler.Repository;
import be.kevinbaes.bap.jmetersampler.TestConfiguration;
import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static be.kevinbaes.bap.jmetersampler.jdbc.JdbcTestConfiguration.INSERT;
import static be.kevinbaes.bap.jmetersampler.jdbc.JdbcTestConfiguration.POOLED;

public class JdbcTest {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcTest.class);

  private TestConfiguration configuration;
  private DataSource dataSource;
  private Repository<?> goalRepository;

  public JdbcTest(JdbcTestConfiguration configuration, ConnectionOptions connectionOptions) {
    this.configuration = configuration;
    LOG.info("initialize JDBC test with connection options: [{}]", connectionOptions);
    JdbcConnectionUtil connectionUtil = new JdbcConnectionUtil(connectionOptions);

    if(this.configuration.getDriverType().equals(POOLED)) {
      this.dataSource = connectionUtil.hikariDataSource();
    } else {
      this.dataSource = connectionUtil.postgresDataSource();
    }

    this.goalRepository = new JdbcRepository(dataSource, configuration);
  }

  public List<?> performDatabaseQueries(SampleResult sampleResult) {
    if(configuration.getQueryType().equals(INSERT)) {
      LOG.info("inserting [{}] times", configuration.getInsertCount());

      goalRepository.insertSequential(sampleResult);
    } else {
      LOG.info("performing select");

      return goalRepository.select(sampleResult);
    }

    return new ArrayList<>();
  }

}
