package examples.r2dbc.raw;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

import java.util.concurrent.atomic.AtomicInteger;

public class R2DBCRenderingStrategy extends RenderingStrategy {

    // see here for how Spring figures this out:
    // https://github.com/spring-projects/spring-data-relational/blob/main/spring-data-r2dbc/src/main/java/org/springframework/data/r2dbc/dialect/DialectResolver.java

    // H2 (Indexed): $1, $2, $3, ... in statement and parameter map
    // MySQL (Anonymous): ?, ?, ? ... in statement, must bind by index
    // Oracle (Named): :P1, :P2, :P3, ... in statement, P1, P2, P3 ... in parameter map
    // Postgres (Indexed): $1, $2, $3, ... in statement and parameter map
    // SQL Server (Named): @P1, @P2, @P3... in statement, P1, P2, P3... in parameter map


    @Override
    public String formatParameterMapKey(AtomicInteger sequence) {
        return "$" + sequence.getAndIncrement(); //$NON-NLS-1$
    }

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return getFormattedJdbcPlaceholder(prefix, parameterName);
    }

    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return parameterName;
    }

    @Override
    public String getRecordBasedInsertBinding(BindableColumn<?> column, String parameterName) {
        return parameterName;
    }
}
