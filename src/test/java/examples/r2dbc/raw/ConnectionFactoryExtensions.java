package examples.r2dbc.raw;

import java.util.Objects;
import java.util.function.BiFunction;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConnectionFactoryExtensions {
    private final ConnectionFactory connectionFactory;

    public ConnectionFactoryExtensions(ConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    public <T> Flux<T> selectMany(SelectStatementProvider selectStatement, BiFunction<Row, RowMetadata, T> rowMapper) {
        return Mono.from(connectionFactory.create())
                .flatMap(c ->
                        Mono.from(R2DBCUtils.createStatement(c, selectStatement).execute())
                        .doFinally(st -> close(c))
                )
                .flatMapMany(result -> Flux.from(result.map(rowMapper)));
    }

    public <T> Mono<T> selectOne(SelectStatementProvider selectStatement, BiFunction<Row, RowMetadata, T> rowMapper) {
        return Mono.from(connectionFactory.create())
                .flatMap(c ->
                        Mono.from(R2DBCUtils.createStatement(c, selectStatement).execute())
                        .doFinally(st -> close(c))
                )
                .flatMap(result -> Mono.from(result.map(rowMapper)));
    }

    public Mono<Long> update(UpdateStatementProvider updateStatement) {
        return Mono.from(connectionFactory.create())
                .flatMap(c ->
                        Mono.from(R2DBCUtils.createStatement(c, updateStatement).execute())
                                .doFinally(st -> close(c))
                )
                .flatMap(result -> Mono.from(result.getRowsUpdated()));
    }

    public Mono<Long> generalInsert(GeneralInsertStatementProvider insertStatement) {
        return Mono.from(connectionFactory.create())
                .flatMap(c ->
                        Mono.from(R2DBCUtils.createStatement(c, insertStatement).execute())
                                .doFinally(st -> close(c))
                )
                .flatMap(result -> Mono.from(result.getRowsUpdated()));
    }

    public Mono<Long> delete(DeleteStatementProvider deleteStatement) {
        return Mono.from(connectionFactory.create())
                .flatMap(c ->
                        Mono.from(R2DBCUtils.createStatement(c, deleteStatement).execute())
                                .doFinally(st -> close(c))
                )
                .flatMap(result -> Mono.from(result.getRowsUpdated()));
    }

    /**
     * This is from <a href="https://github.com/eugenp/tutorials/tree/master/persistence-modules/r2dbc">...</a>.
     *
     * Not sure why we need it.
     */
    private <T> Mono<T> close(Connection connection) {
        return Mono.from(connection.close())
                .then(Mono.empty());
    }
}
