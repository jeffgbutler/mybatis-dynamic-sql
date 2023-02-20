package examples.r2dbc.raw;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

import java.util.Map;

public class R2DBCUtils {
    private R2DBCUtils() {}

    public static Statement createStatement(Connection connection, SelectStatementProvider selectStatement) {
        return createStatement(connection, selectStatement.getSelectStatement(), selectStatement.getParameters());
    }

    public static Statement createStatement(Connection connection, DeleteStatementProvider deleteStatement) {
        return createStatement(connection, deleteStatement.getDeleteStatement(), deleteStatement.getParameters());
    }

    public static Statement createStatement(Connection connection, UpdateStatementProvider updateStatement) {
        return createStatement(connection, updateStatement.getUpdateStatement(), updateStatement.getParameters());
    }

    public static Statement createStatement(Connection connection, GeneralInsertStatementProvider insertStatement) {
        return createStatement(connection, insertStatement.getInsertStatement(), insertStatement.getParameters());
    }

    private static Statement createStatement(Connection connection, String sql, Map<String, Object> parameters) {
        Statement statement = connection.createStatement(sql);

        parameters.forEach(statement::bind);

        return statement;
    }
}
