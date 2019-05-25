package be.kevinbaes.bap.jmetersampler;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

/**
 * Reduce deplicate constants and setup code from the samplers.
 */
class SamplerSetupUtil {

  static final String DRIVER_TYPE_PARAM = "Driver type";
  static final String QUERY_TYPE_PARAM = "Query type";
  static final String INSERT_COUNT_PARAM = "Insert count";
  static final String RETRY_COUNT_PARAM = "Retry count";
  static final String RETRY_DELAY_PARAM = "Retry delay";

  private static final String USERNAME_PARAM = "Username";
  private static final String PASSWORD_PARAM = "Password";
  private static final String HOST_PARAM = "Host";
  private static final String PORT_PARAM = "Port";
  private static final String DATABASE_PARAM = "Database";

  private static final String USERNAME_DEFAULT = "${username}";
  private static final String PASSWORD_DEFAULT = "${password}";
  private static final String HOST_DEFAULT = "${host}";
  private static final String PORT_DEFAULT = "${port}";
  private static final String DATABASE_DEFAULT = "${database}";

  /**
   * Creates arguments with database connection settings
   * @return Arguments
   */
  Arguments defaultArguments() {
    Arguments params = new Arguments();

    params.addArgument(USERNAME_PARAM, USERNAME_DEFAULT);
    params.addArgument(PASSWORD_PARAM, PASSWORD_DEFAULT);
    params.addArgument(HOST_PARAM, HOST_DEFAULT);
    params.addArgument(PORT_PARAM, PORT_DEFAULT);
    params.addArgument(DATABASE_PARAM, DATABASE_DEFAULT);

    return params;
  }

  /**
   * reads database connection options from the jmeter parameters
   */
  ConnectionOptions connectionOptions(JavaSamplerContext context) {
    String username = context.getParameter(USERNAME_PARAM);
    String password = context.getParameter(PASSWORD_PARAM);
    int port = context.getIntParameter(PORT_PARAM);
    String host = context.getParameter(HOST_PARAM);
    String database = context.getParameter(DATABASE_PARAM);

    return new ConnectionOptions(username, password, port, database, host);
  }
}
