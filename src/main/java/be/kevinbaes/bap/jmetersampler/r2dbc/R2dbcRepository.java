package be.kevinbaes.bap.jmetersampler.r2dbc;

import be.kevinbaes.bap.jmetersampler.domain.DeviceEvent;
import io.r2dbc.spi.*;
import org.apache.jmeter.samplers.SampleResult;
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

  public Mono<Void> insertInterleaved(int insertCount, int retryCount, int retryDelay, SampleResult sampleResult) {
    final Boolean[] first = new Boolean[]{true};

    return Flux.range(1, insertCount)
        .flatMap(j ->
            queryUtil.executeStatement(
              conn -> {
                if(first[0]){
                  sampleResult.connectEnd();
                  first[0] = false;
                }
                Statement stmt = conn.createStatement("insert into goal (name) values ('test')");

                return Flux.from(stmt.execute()).flatMap(Result::getRowsUpdated);
              }
            )
              .retryBackoff(retryCount, Duration.ofMillis(retryDelay), Duration.ofMillis(1000), .5)
        )
        .then();
  }

  /**
   * This is basically using R2DBC like JDBC: doing sequential requests, waiting for the previous query to complete
   * before doing the next.
   */
  public Mono<Void> insertSequential(int count, int retryCount, int retryDelay, SampleResult result) {
    Mono<Void> allInserts = Mono.empty();
    final Boolean[] first = new Boolean[]{true};
    for (int i = 0; i < count; i++) {
      allInserts = allInserts.then(
          Mono.from(queryUtil.executeStatement(connection -> {
            if(first[0]) {
              result.connectEnd();
              first[0] = false;
            }
            return insertOne(connection);
          })).then()
      );
    }
    return allInserts;
  }

  private Publisher<? extends Result> insertOne(Connection conn) {
    return conn.createStatement("INSERT INTO goal (name) VALUES ($1)")
        .bind("$1", createRandomString())
        .execute();
  }


  /**
   * @return a some string
   */
  private String createRandomString() {
    StringBuilder builder = new StringBuilder();
    builder.append("goal");
    return builder.toString();
  }

  public Flux<DeviceEvent> select(SampleResult sampleResult) {
    return Mono.from(connectionFactory.create())
        .flatMapMany(conn -> {
              sampleResult.connectEnd();

              return Flux.from(conn.createStatement("select * from device_event;")
                  .execute())
                  .flatMap(result -> result.map(this::rowToDeviceEvent))
                  .concatWith(Mono.from(conn.close()).then(Mono.empty()))
                  .onErrorResume(e -> Mono.from(conn.close()).then(Mono.error(e)));
        });
  }

  private DeviceEvent rowToDeviceEvent(Row row, RowMetadata metadata) {
    int id = row.get("id", Integer.class);
    long receivedTimeMs = row.get("received_time", Long.class);
    long lattitude = row.get("lattitude", Long.class);
    long longitude = row.get("longitude", Long.class);

    return new DeviceEvent(id, new Date(receivedTimeMs), lattitude, longitude);
  }

}
