package be.kevinbaes.bap.jmetersampler;

import be.kevinbaes.bap.jmetersampler.jdbc.JdbcTest;
import be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcTest;

public abstract class TestConfiguration {

  public static final String INSERT = "insert";
  public static final String INSERT_INTERLEAVE = "insert interleave";
  public static final String SELECT = "select";

  public static final String POOLED = "pooled";
  public static final String UNPOOLED = "unpooled";

  private final String driverType;
  private final String queryType;
  private final int selectCount;
  private final int insertCount;

  /**
   * Create a TestConfiguration for {@link JdbcTest} and {@link R2dbcTest}
   * @param driverType  The driver type. Is pooled or unpooled, signalling that you might want to build a pool.
   * Not sure where the 'driver' enters the story.
   * @param queryType Use this to signal the type of test to perform
   * @param selectCount Use this to set a limit clause on your query.
   * @param insertCount Use to perform a number of inserts.
   */
  public TestConfiguration(String driverType, String queryType, int selectCount, int insertCount) {
    this.driverType = driverType;
    this.queryType = queryType;
    this.selectCount = selectCount;
    this.insertCount = insertCount;
  }

  public String getQueryType() {
    return queryType;
  }

  public String getDriverType() {
    return driverType;
  }

  public int getSelectCount() {
    return selectCount;
  }

  public int getInsertCount() {
    return insertCount;
  }

}
