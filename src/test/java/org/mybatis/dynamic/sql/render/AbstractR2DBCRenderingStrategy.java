package org.mybatis.dynamic.sql.render;

import org.mybatis.dynamic.sql.BindableColumn;

// see here for how Spring figures this out:
// https://github.com/spring-projects/spring-data-relational/blob/main/spring-data-r2dbc/src/main/java/org/springframework/data/r2dbc/dialect/DialectResolver.java

// H2 (Indexed): $1, $2, $3, ... in statement and parameter map
// MySQL (Anonymous): ?, ?, ? ... in statement, must bind by index
// Oracle (Named): :P1, :P2, :P3, ... in statement, P1, P2, P3 ... in parameter map
// Postgres (Indexed): $1, $2, $3, ... in statement and parameter map
// SQL Server (Named): @P1, @P2, @P3... in statement, P1, P2, P3... in parameter map

public abstract class AbstractR2DBCRenderingStrategy  extends RenderingStrategy{
    @Override
    public final String getRecordBasedInsertBinding(BindableColumn<?> column, String parameterName) {
        // TODO - externalize message
        throw new UnsupportedOperationException("R2DBC Does Not Support Record Based Parameters"); //$NON-NLS-1$
    }

    @Override
    public final String getRecordBasedInsertBinding(BindableColumn<?> column, String prefix, String parameterName) {
        // TODO - externalize message
        throw new UnsupportedOperationException("R2DBC Does Not Support Record Based Parameters"); //$NON-NLS-1$
    }
}
