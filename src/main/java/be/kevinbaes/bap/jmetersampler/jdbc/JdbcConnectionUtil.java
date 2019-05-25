package be.kevinbaes.bap.jmetersampler.jdbc;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class JdbcConnectionUtil {
  private final ConnectionOptions connectionOptions;

  public JdbcConnectionUtil(ConnectionOptions connectionOptions) {
    this.connectionOptions = connectionOptions;
  }

  public DataSource postgresDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();

    dataSource.setUrl(getJdbcUrl());
    dataSource.setUser(connectionOptions.getUsername());
    dataSource.setPassword(connectionOptions.getPassword());

    return dataSource;
  }

  public DataSource hikariDataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl( getJdbcUrl() );
    config.setUsername( "postgres" );
    config.setPassword( "postgres" );
    config.setMaximumPoolSize(4);

    HikariDataSource hikariDataSource = new HikariDataSource(config);

    return hikariDataSource;
  }

  // TODO discuss driver url differences in paper
  private String getJdbcUrl() {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append("jdbc:postgresql://");
    urlBuilder.append(connectionOptions.getHost());
    urlBuilder.append(":");
    urlBuilder.append(connectionOptions.getPort());
    urlBuilder.append("/");
    urlBuilder.append(connectionOptions.getDatabase());
    return urlBuilder.toString();
  }

}
