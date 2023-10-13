package org.mybatis.dynamic.sql.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

import java.sql.JDBCType;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class R2DBCRenderingTest {
    private final SqlTable foo = SqlTable.of("foo");
    private final SqlColumn<Integer> id = foo.column("id", JDBCType.INTEGER);
    private final SqlColumn<String> description = foo.column("description", JDBCType.VARCHAR);
    private final RenderingStrategy ORACLE = new OracleR2DBCRenderingStrategy();
    private final RenderingStrategy POSTGRES = new PostgresR2DBCRenderingStrategy();
    private final RenderingStrategy H2 = new H2R2DBCRenderingStrategy();
    private final RenderingStrategy MSSQL = new SqlServerR2DBCRenderingStrategy();
    private final RenderingStrategy MYSQL = new MySQLR2DBCRenderingStrategy();

    @Test
    void testOracle() {
        SelectStatementProvider selectStatement = select(id, description)
                .from(foo)
                .where(id, isEqualTo(4))
                .build()
                .render(ORACLE);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, description from foo where id = :p1");
        assertThat(selectStatement.getParameters()).containsEntry("p1", 4);
    }

    @Test
    void testPostgres() {
        SelectStatementProvider selectStatement = select(id, description)
                .from(foo)
                .where(id, isEqualTo(4))
                .build()
                .render(POSTGRES);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, description from foo where id = $1");
        assertThat(selectStatement.getParameters()).containsEntry("$1", 4);
    }

    @Test
    void testH2() {
        SelectStatementProvider selectStatement = select(id, description)
                .from(foo)
                .where(id, isEqualTo(4))
                .build()
                .render(H2);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, description from foo where id = $1");
        assertThat(selectStatement.getParameters()).containsEntry("$1", 4);
    }

    @Test
    void testSqlServer() {
        SelectStatementProvider selectStatement = select(id, description)
                .from(foo)
                .where(id, isEqualTo(4))
                .build()
                .render(MSSQL);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, description from foo where id = @p1");
        assertThat(selectStatement.getParameters()).containsEntry("p1", 4);
    }

    @Test
    void testMySql() {
        SelectStatementProvider selectStatement = select(id, description)
                .from(foo)
                .where(id, isIn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22))
                .build()
                .render(MYSQL);

        assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, description from foo where id in (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        AnonymousParameters parameters = new AnonymousParameters(selectStatement.getParameters());

        // test that parameters are in numeric order...
        int i = 1;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            assertThat(entry.getKey()).isEqualTo(Integer.toString(i));
            assertThat(entry.getValue()).isEqualTo(i);
            i++;
        }
    }

    public static class AnonymousParameters extends TreeMap<String, Object> {
        public AnonymousParameters(Map<String, Object> parameters) {
            super(Comparator.comparing(Integer::valueOf));
            putAll(parameters);
        }
    }
}
