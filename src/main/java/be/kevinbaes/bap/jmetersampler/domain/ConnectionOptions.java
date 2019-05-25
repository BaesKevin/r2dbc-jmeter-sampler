package be.kevinbaes.bap.jmetersampler.domain;

/**
 * Connectionoptions used by connection util classes to create a DataSource or ConnectionFactory
 */
public class ConnectionOptions {
  private String username;
  private String password;
  private int port;
  private String database;
  private String host;

  public ConnectionOptions(String username, String password, int port, String database, String host) {
    this.username = username;
    this.password = password;
    this.port = port;
    this.database = database;
    this.host = host;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public int getPort() {
    return port;
  }

  public String getDatabase() {
    return database;
  }

  public String getHost() {
    return host;
  }

  @Override
  public String toString() {
    return "ConnectionOptions{" +
        "username='" + username + '\'' +
        ", port=" + port +
        ", database='" + database + '\'' +
        ", host='" + host + '\'' +
        '}';
  }
}
