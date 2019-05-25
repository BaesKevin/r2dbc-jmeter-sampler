package be.kevinbaes.bap.jmetersampler;

public abstract class TestConfiguration {

  public static final String INSERT = "insert";
  public static final String INSERT_INTERLEAVE = "insert interleave";
  public static final String SELECT = "select";

  public static final String POOLED = "pooled";
  public static final String UNPOOLED = "unpooled";

  private final String driverType;
  private final String queryType;
  private final int insertCount;

  public TestConfiguration(String driverType, String queryType, int insertCount) {
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
