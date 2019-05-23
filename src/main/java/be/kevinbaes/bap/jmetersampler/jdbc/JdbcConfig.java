package be.kevinbaes.bap.jmetersampler.jdbc;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class JdbcConfig {

  public DataSource postgresDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();

    dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres");
    dataSource.setUser("postgres");
    dataSource.setPassword("postgres");

    return dataSource;
  }

}
