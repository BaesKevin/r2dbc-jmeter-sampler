package be.kevinbaes.bap.jmetersampler.jdbc;

public class JdbcTestConfiguration {
  /**TODO find number of threads requried in jmeter to reach R2DBC throughput*/
  public static final String INSERT = "insert";
  public static final String SELECT = "select";

  public static final String POOLED = "pooled";
  public static final String UNPOOLED = "unpooled";

  private final String driverType;
  private final String queryType;
  private final int insertCount;

  public JdbcTestConfiguration(String driverType, String queryType, int insertCount) {
    this.driverType = driverType;
    this.queryType = queryType;
    this.insertCount = insertCount;
  }

  public String getQueryType() {
    return queryType;
  }

  public int getInsertCount() {
    return insertCount;
  }

  public String getDriverType() {
    return driverType;
  }

}
