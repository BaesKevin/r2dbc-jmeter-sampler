package be.kevinbaes.bap.jmetersampler;

import be.kevinbaes.bap.jmetersampler.domain.ConnectionOptions;
import be.kevinbaes.bap.jmetersampler.r2dbc.QueryUtil;
import be.kevinbaes.bap.jmetersampler.r2dbc.R2dbcConnectionUtil;
import io.r2dbc.spi.Batch;
import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility runner class which can be used to seed 100k events into the device_event table, does so almost instantly.
 */
public class SeedDeviceEvent {

  private QueryUtil queryUtil;

  public static void main(String[] args) {
    new SeedDeviceEvent().run();
  }

  private void run() {
    ConnectionOptions connectionOptions = new ConnectionOptions("postgres", "postgres", 5432, "postgres", "localhost");
    R2dbcConnectionUtil r2dbcConnectionUtil = new R2dbcConnectionUtil(connectionOptions);
    queryUtil = new QueryUtil(r2dbcConnectionUtil.postgresConnectionFactory());

    batchInsert();
  }

  private void batchInsert() {
    Mono<Integer> rowsUpdatedAfterBatchInsert = queryUtil.executeStatement(
        (connection) -> createInsertBatch(connection).execute())
          .flatMap(result -> Mono.from(result.getRowsUpdated()))
          .reduce(0, Integer::sum);

    System.out.println("rows updated after batch: " + rowsUpdatedAfterBatchInsert.block());

  }

  /**
   * create a batch of ten insert querys
   */
  private Batch createInsertBatch(Connection connection) {
    Batch batch = connection.createBatch();

    ThreadLocalRandom random =ThreadLocalRandom.current();

    int count = 100000;

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder
        .append("INSERT INTO device_event (received_time, lattitude, longitude) VALUES ");

    for (int i = 0; i < count; i++) {
      long receivedTime = random.nextInt(0, Integer.MAX_VALUE);
      long lattitude = random.nextInt(-90, 90);
      long longitude = random.nextInt(-180, 180);

      queryBuilder.append("(").append(receivedTime).append(",").append(lattitude).append(",").append(longitude).append(")");
      if(i < count - 1) {
        queryBuilder.append(",");
      }
    }

    batch.add(queryBuilder.toString());

    return batch;
  }
}
