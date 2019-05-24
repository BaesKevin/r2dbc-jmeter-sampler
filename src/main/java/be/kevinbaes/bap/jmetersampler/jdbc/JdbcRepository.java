package be.kevinbaes.bap.jmetersampler.jdbc;

import be.kevinbaes.bap.jmetersampler.domain.DeviceEvent;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcRepository {

  private final DataSource dataSource;

  public JdbcRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<DeviceEvent> select() throws SQLException {
    List<DeviceEvent> events = new ArrayList<>();
    Connection conn = null;
    try {

      conn = dataSource.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet resultSet = null;

      try {
        resultSet = stmt.executeQuery("select * from device_event");

        while (resultSet.next()) {
          int id = resultSet.getInt("id");
          long receivedTime = resultSet.getLong("received_time");
          long lattitude = resultSet.getLong("lattitude");
          long longitude = resultSet.getLong("longitude");

          events.add(new DeviceEvent(id, new Date(receivedTime), lattitude, longitude));
        }

        resultSet.close();
        stmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (stmt != null) {
          stmt.close();
        }
        if (resultSet != null) {
          resultSet.close();
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

    return events;
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
