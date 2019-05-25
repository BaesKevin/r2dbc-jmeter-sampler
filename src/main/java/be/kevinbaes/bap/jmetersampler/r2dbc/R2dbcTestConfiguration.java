package be.kevinbaes.bap.jmetersampler.r2dbc;

import be.kevinbaes.bap.jmetersampler.TestConfiguration;

public class R2dbcTestConfiguration extends TestConfiguration {
  private final int retryCount;
  private final int retryDelay;

  public R2dbcTestConfiguration(String driverType, String queryType, int insertCount, int retryCount, int retryDelay) {
    super(driverType, queryType, insertCount);
    this.retryCount = retryCount;
    this.retryDelay = retryDelay;
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
        "driverType='" + getDriverType() + '\'' +
        ", queryType='" + getQueryType() + '\'' +
        ", insertCount=" + getInsertCount() +
        ", retryCount=" + retryCount +
        ", retryDelay=" + retryDelay +
    '}';
  }

}
