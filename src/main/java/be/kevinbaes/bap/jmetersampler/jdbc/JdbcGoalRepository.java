package be.kevinbaes.bap.jmetersampler.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcGoalRepository {

  private final DataSource dataSource;

  public JdbcGoalRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void select() {

  }

  public void insert(int count) throws SQLException {

    Connection conn = null;
    try {

      conn = dataSource.getConnection();

      for (int i = 0; i < count; i++) {
        PreparedStatement stmt = null;
        try {
          stmt = conn.prepareStatement("insert into goal (name) values (?)");
          stmt.setString(1, "goal" + i);

          int updateCount = stmt.executeUpdate();

          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          if (stmt != null) {
            stmt.close();
          }
        }
      }

      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }
}
