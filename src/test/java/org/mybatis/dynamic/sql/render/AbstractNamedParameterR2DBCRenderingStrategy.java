package org.mybatis.dynamic.sql.render;

import org.mybatis.dynamic.sql.BindableColumn;

public class AbstractNamedParameterR2DBCRenderingStrategy extends AbstractR2DBCRenderingStrategy {
    private final String pfx;

    protected AbstractNamedParameterR2DBCRenderingStrategy(String prefix) {
        this.pfx = prefix;
    }

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return pfx + parameterName;
    }

    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return pfx + parameterName;
    }
}
