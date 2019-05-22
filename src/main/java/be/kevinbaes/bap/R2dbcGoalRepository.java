package be.kevinbaes.bap;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class R2dbcGoalRepository {
  private final ConnectionFactory connectionFactory;

  public R2dbcGoalRepository(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public Mono<Void> insert() {
    return Mono.from(connectionFactory.create())
        .flatMapMany(conn ->
            Flux.from(conn.createStatement("INSERT INTO goal (name) VALUES ($1),($2)")
                .bind("$1", "Create an application")
                .bind("$2", "Create a second application")
                .execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .concatWith(Mono.from(conn.close()).then(Mono.empty()))
                .onErrorResume(e -> Mono.from(conn.close()).then(Mono.error(e)))
        ).then();
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
