package be.kevinbaes.bap.jmetersampler.jdbc;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import be.kevinbaes.bap.jmetersampler.domain.DeviceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static be.kevinbaes.bap.jmetersampler.jdbc.JdbcTestConfiguration.INSERT;
import static be.kevinbaes.bap.jmetersampler.jdbc.JdbcTestConfiguration.POOLED;

public class JdbcTest {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcTest.class);

  private JdbcTestConfiguration configuration;
  private DataSource dataSource;
  private JdbcRepository goalRepository;

  public JdbcTest(JdbcTestConfiguration configuration, ConnectionOptions connectionOptions) {
    this.configuration = configuration;
    JdbcConnectionUtil connectionUtil = new JdbcConnectionUtil(connectionOptions);

    if(this.configuration.getDriverType().equals(POOLED)) {
      this.dataSource = connectionUtil.hikariDataSource();
    } else {
      this.dataSource = connectionUtil.postgresDataSource();
    }

    this.goalRepository = new JdbcRepository(dataSource);
  }

  public List<DeviceEvent> performDatabaseQueries() throws SQLException {
    if(configuration.getQueryType().equals(INSERT)) {
      LOG.info("inserting [{}] times", configuration.getInsertCount());

      goalRepository.insert(configuration.getInsertCount());
    } else {
      LOG.info("performing select");

      return goalRepository.select();
    }

    return new ArrayList<>();
  }

}
