package be.kevinbaes.bap.jmetersampler.jdbc;

import be.kevinbaes.bap.jmetersampler.Repository;
import be.kevinbaes.bap.jmetersampler.domain.DeviceEvent;
import org.apache.jmeter.samplers.SampleResult;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcRepository implements Repository<DeviceEvent> {

  private final DataSource dataSource;
  private final JdbcTestConfiguration jdbcTestConfiguration;

  JdbcRepository(DataSource dataSource, JdbcTestConfiguration jdbcTestConfiguration) {
    this.dataSource = dataSource;
    this.jdbcTestConfiguration = jdbcTestConfiguration;
  }

  public List<DeviceEvent> select(SampleResult sampleResult) {
    List<DeviceEvent> events = new ArrayList<>();
    Connection conn = null;
    try {

      conn = dataSource.getConnection();
      sampleResult.connectEnd();

      String query = "select * from device_event limit " + jdbcTestConfiguration.getSelectCount();
      try (Statement stmt = conn.createStatement(); ResultSet resultSet = stmt.executeQuery(query)) {

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
      }

      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try{
          conn.close();
        } catch (SQLException s) {
          // rethrow as to not swallow exceptions
          throw new RuntimeException(s);
        }
      }
    }

    return events;
  }

  public void insertSequential(SampleResult sampleResult) {

    Connection conn = null;
    try {

      conn = dataSource.getConnection();
      sampleResult.connectEnd();
      for (int i = 0; i < jdbcTestConfiguration.getInsertCount(); i++) {
        try (PreparedStatement stmt = conn.prepareStatement("insert into goal (name) values (?)")) {
          stmt.setString(1, "goal" + i);

          int updateCount = stmt.executeUpdate();

          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try{
          conn.close();
        } catch (SQLException s) {
          // rethrow as to not swallow exceptions
          throw new RuntimeException(s);
        }
      }
    }
  }

  @Override
  public void insertInterleaved(SampleResult sampleResult) {
    throw new NotImplementedException();
  }
}
