package be.kevinbaes.bap.jmetersampler.r2dbc;

public class R2dbcTestConfiguration {
  public static final String INSERT = "insert";
  public static final String INSERT_INTERLEAVE = "insert interleave";
  public static final String SELECT = "select";

  public static final String POOLED = "pooled";
  public static final String UNPOOLED = "unpooled";

  private final String driverType;
  private final String queryType;
  private final int insertCount;

  public R2dbcTestConfiguration(String driverType, String queryType, int insertCount) {
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

  @Override
  public String toString() {
    return "R2dbcTestConfiguration{" +
        "driverType='" + driverType + '\'' +
        ", queryType='" + queryType + '\'' +
        ", insertCount=" + insertCount +
        '}';
  }
}
