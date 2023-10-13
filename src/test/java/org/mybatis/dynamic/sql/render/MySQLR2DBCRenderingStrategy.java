package org.mybatis.dynamic.sql.render;

import org.mybatis.dynamic.sql.BindableColumn;

import java.util.concurrent.atomic.AtomicInteger;

public class MySQLR2DBCRenderingStrategy extends AbstractR2DBCRenderingStrategy {
    // MySQL (Anonymous): ?, ?, ? ... in statement, must bind by index

    // TODO - not supported by our current rendering strategy!

    @Override
    public String formatParameterMapKey(AtomicInteger sequence) {
        return Integer.toString(sequence.getAndIncrement());
    }

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return "?"; //$NON-NLS-1$
    }

    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return "?"; //$NON-NLS-1$
    }
}
