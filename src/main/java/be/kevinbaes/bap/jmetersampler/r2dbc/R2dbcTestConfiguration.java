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
  private final int retryCount;
  private final int retryDelay;

  public R2dbcTestConfiguration(String driverType, String queryType, int insertCount, int retryCount, int retryDelay) {
    this.driverType = driverType;
    this.queryType = queryType;
    this.insertCount = insertCount;
    this.retryCount = retryCount;
    this.retryDelay = retryDelay;
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

  public int getRetryCount() {
    return retryCount;
  }

  public int getRetryDelay() {
    return retryDelay;
  }

  @Override
  public String toString() {
    return "R2dbcTestConfiguration{" +
        "driverType='" + driverType + '\'' +
        ", queryType='" + queryType + '\'' +
        ", insertCount=" + insertCount +
        ", retryCount=" + retryCount +
        ", retryDelay=" + retryDelay +
    '}';
  }

}
