package examples.r2dbc.raw;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

import java.util.concurrent.atomic.AtomicInteger;

public class R2DBCRenderingStrategy extends RenderingStrategy {

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
