package be.kevinbaes.bap.jmetersampler.r2dbc;

import be.kevinbaes.bap.jmetersampler.domain.DeviceEvent;
import io.r2dbc.spi.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

public class R2dbcRepository {
  private final ConnectionFactory connectionFactory;
  private final QueryUtil queryUtil;

  public R2dbcRepository(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
    this.queryUtil = new QueryUtil(this.connectionFactory);
  }

  public Mono<Void> insertInterleaved(int insertCount, int retryCount, int retryDelay) {
    return Flux.range(1, insertCount)
        .flatMap(j ->
            queryUtil.executeStatement(
              conn -> {
                Statement stmt = conn.createStatement("insert into goal (name) values ('test')");

                return Flux.from(stmt.execute()).flatMap(Result::getRowsUpdated);
              }
            )
              .retryBackoff(retryCount, Duration.ofMillis(retryDelay), Duration.ofMillis(1000), .5)
        )
        .then();
  }

  public Mono<Void> insertSingleConnection(int count, int retryCount, int retryDelay) {
    return Flux.from(connectionFactory.create())
        .retryBackoff(retryCount, Duration.ofMillis(retryDelay), Duration.ofMillis(1000), .5)
        .flatMap(
            conn -> queryUtil.executeStatement(c -> doMultipleInserts(conn, count))
        ).then();

  }

  private Mono<Void> doMultipleInserts(Connection connection, int count) {
    Mono<Void> allInserts = Mono.empty();

    for (int i = 0; i < count; i++) {
      allInserts = allInserts
          .then(
              Flux
                  .from(insertOne(connection, i))
                  .flatMap(result -> Mono.from(result.getRowsUpdated()))
                  .then()
          );
    }

    return allInserts;
  }

  private Publisher<? extends Result> insertOne(Connection conn, int i) {
    return conn.createStatement("INSERT INTO goal (name) VALUES ($1)")
        .bind("$1", createRandomString(i))
        .execute();
  }


  /**
   * @return a some string
   */
  private String createRandomString(int i) {
    StringBuilder builder = new StringBuilder();
    builder.append("goal-");
    builder.append(i);
    return builder.toString();
  }

  public Flux<DeviceEvent> select() {
    return Mono.from(connectionFactory.create())
        .flatMapMany(conn ->
            Flux.from(conn.createStatement("select * from device_event;")
                .execute())
                .flatMap(result -> result.map(this::rowToDeviceEvent))
                .concatWith(Mono.from(conn.close()).then(Mono.empty()))
                .onErrorResume(e -> Mono.from(conn.close()).then(Mono.error(e)))
        );
  }

  private DeviceEvent rowToDeviceEvent(Row row, RowMetadata metadata) {
    int id = row.get("id", Integer.class);
    long receivedTimeMs = row.get("received_time", Long.class);
    long lattitude = row.get("received_time", Long.class);
    long longitude = row.get("received_time", Long.class);

    return new DeviceEvent(id, new Date(receivedTimeMs), lattitude, longitude);
  }

}
