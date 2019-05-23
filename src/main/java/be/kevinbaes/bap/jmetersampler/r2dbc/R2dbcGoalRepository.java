package be.kevinbaes.bap.jmetersampler.r2dbc;

import be.kevinbaes.bap.jmetersampler.domain.Goal;
import io.r2dbc.spi.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class R2dbcGoalRepository {
  private final ConnectionFactory connectionFactory;

  public R2dbcGoalRepository(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public Mono<Void> insertInterleaved(int count) {
    QueryUtil queryUtil = new QueryUtil(this.connectionFactory);
    return Flux.range(1, count)
        .flatMap(j -> queryUtil.executeStatement(
            conn -> {
              Statement stmt = conn.createStatement("insert into goal (name) values ('test')");

              return Flux.from(stmt.execute()).flatMap(Result::getRowsUpdated);
            }
        ))
        .then();
  }

  public Mono<Void> insertSingleConnection(int count) {
    return Mono.from(connectionFactory.create())
        .flatMapMany(conn ->
            doMultipleInserts(conn, count)
                .concatWith(Mono.from(conn.close()).then(Mono.empty()))
                .onErrorResume(e -> Mono.from(conn.close()).then(Mono.error(e)))
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
   * Implementation from https://www.baeldung.com/java-random-string
   *
   * @return a random string of length 10
   */
  private String createRandomString(int i) {
    StringBuilder builder = new StringBuilder();
    builder.append(Thread.currentThread().getName());
    builder.append("-value-");
    builder.append(i);
    return builder.toString();
  }

  public Mono<Void> select() {
    return Mono.from(connectionFactory.create())
        .flatMapMany(conn ->
            Flux.from(conn.createStatement("select * from goal;")
                .execute())
                .flatMap(result -> result.map(this::rowToGoal))
                .concatWith(Mono.from(conn.close()).then(Mono.empty()))
                .onErrorResume(e -> Mono.from(conn.close()).then(Mono.error(e)))
        ).then();
  }

  private Goal rowToGoal(Row row, RowMetadata metadata) {
    return new Goal(
        row.get("id", Integer.class),
        row.get("name", String.class))
        ;
  }

}
