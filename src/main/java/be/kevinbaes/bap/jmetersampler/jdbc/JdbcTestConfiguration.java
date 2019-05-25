package be.kevinbaes.bap.jmetersampler.jdbc;

import be.kevinbaes.bap.jmetersampler.TestConfiguration;

public class JdbcTestConfiguration extends TestConfiguration {
  public JdbcTestConfiguration(String driverType, String queryType, int selectCount, int insertCount) {
    super(driverType, queryType, selectCount, insertCount);
  }
}
