package be.kevinbaes.bap.jmetersampler.jdbc;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

class JdbcConnectionUtil {
  private final ConnectionOptions connectionOptions;

  JdbcConnectionUtil(ConnectionOptions connectionOptions) {
    this.connectionOptions = connectionOptions;
  }

  DataSource postgresDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();

    dataSource.setUrl(getJdbcUrl());
    dataSource.setUser(connectionOptions.getUsername());
    dataSource.setPassword(connectionOptions.getPassword());

    return dataSource;
  }

  DataSource hikariDataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl( getJdbcUrl() );
    config.setUsername( connectionOptions.getUsername() );
    config.setPassword( connectionOptions.getPassword() );
    config.setMaximumPoolSize(10);

    return new HikariDataSource(config);
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
