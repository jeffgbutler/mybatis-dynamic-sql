package examples.r2dbc.spring;

import java.util.Map;
import java.util.function.BiFunction;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class R2dbcMyBatisDSQLTemplate {
    private final DatabaseClient databaseClient;

    public R2dbcMyBatisDSQLTemplate(ConnectionFactory connectionFactory) {
        databaseClient = DatabaseClient.create(connectionFactory);
    }

    public Mono<Long> delete(Buildable<DeleteModel> deleteModel) {
        DeleteStatementProvider deleteStatement = deleteModel.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        GenericExecuteSpec spec = databaseClient.sql(deleteStatement.getDeleteStatement());

        return bindParameters(spec, deleteStatement.getParameters()).fetch().rowsUpdated();
    }

    public GenericExecuteSpec prepareGeneralInsert(Buildable<GeneralInsertModel> insertModel) {
        GeneralInsertStatementProvider insertStatement = insertModel.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        GenericExecuteSpec spec = databaseClient.sql(insertStatement.getInsertStatement());

        return bindParameters(spec, insertStatement.getParameters());
    }

    public Mono<Long> generalInsert(Buildable<GeneralInsertModel> insertModel) {
        return prepareGeneralInsert(insertModel).fetch().rowsUpdated();
    }

    public <R> Mono<R> selectOne(Buildable<SelectModel> selectModel, BiFunction<Row, RowMetadata, R> rowMapper) {
        SelectStatementProvider selectStatement = selectModel.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        GenericExecuteSpec spec = databaseClient.sql(selectStatement.getSelectStatement());

        return bindParameters(spec, selectStatement.getParameters()).map(rowMapper).one();
    }

    public Mono<Map<String, Object>> selectOne(Buildable<SelectModel> selectModel) {
        SelectStatementProvider selectStatement = selectModel.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        GenericExecuteSpec spec = databaseClient.sql(selectStatement.getSelectStatement());

        return bindParameters(spec, selectStatement.getParameters()).map(R2DBCUtils::rawMapper).one();
    }

    public <R> Flux<R> selectMany(Buildable<SelectModel> selectModel, BiFunction<Row, RowMetadata, R> rowMapper) {
        SelectStatementProvider selectStatement = selectModel.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        GenericExecuteSpec spec = databaseClient.sql(selectStatement.getSelectStatement());

        return bindParameters(spec, selectStatement.getParameters()).map(rowMapper).all();
    }

    public Flux<Map<String, Object>> selectMany(Buildable<SelectModel> selectModel) {
        SelectStatementProvider selectStatement = selectModel.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        GenericExecuteSpec spec = databaseClient.sql(selectStatement.getSelectStatement());

        return bindParameters(spec, selectStatement.getParameters()).map(R2DBCUtils::rawMapper).all();
    }

    public GenericExecuteSpec prepareUpdate(Buildable<UpdateModel> updateModel) {
        UpdateStatementProvider updateStatement = updateModel.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        GenericExecuteSpec spec = databaseClient.sql(updateStatement.getUpdateStatement());

        return bindParameters(spec, updateStatement.getParameters());
    }

    public Mono<Long> update(Buildable<UpdateModel> updateModel) {
        return prepareUpdate(updateModel).fetch().rowsUpdated();
    }

    private GenericExecuteSpec bindParameters(GenericExecuteSpec spec, Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }

        return spec;
    }
}
