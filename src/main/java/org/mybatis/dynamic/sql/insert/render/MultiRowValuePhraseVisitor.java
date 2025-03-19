/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.insert.render;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.MultiRowInsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.RowMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class MultiRowValuePhraseVisitor extends MultiRowInsertMappingVisitor<FieldAndValue> {
    protected final RenderingStrategy renderingStrategy;
    protected final String prefix;

    protected MultiRowValuePhraseVisitor(RenderingStrategy renderingStrategy, String prefix) {
        this.renderingStrategy = renderingStrategy;
        this.prefix = prefix;
    }

    @Override
    public FieldAndValue visit(NullMapping mapping) {
        return new FieldAndValue(mapping.columnName(), "null"); //$NON-NLS-1$
    }

    @Override
    public FieldAndValue visit(ConstantMapping mapping) {
        return new FieldAndValue(mapping.columnName(), mapping.constant());
    }

    @Override
    public FieldAndValue visit(StringConstantMapping mapping) {
        return new FieldAndValue(mapping.columnName(), StringUtilities.formatConstantForSQL(mapping.constant()));
    }

    @Override
    public FieldAndValue visit(PropertyMapping mapping) {
        return new FieldAndValue(mapping.columnName(), calculateJdbcPlaceholder(mapping.column(), mapping.property()));
    }

    @Override
    public FieldAndValue visit(RowMapping mapping) {
        return new FieldAndValue(mapping.columnName(), calculateJdbcPlaceholder(mapping.column()));
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column) {
        return column.renderingStrategy().orElse(renderingStrategy).getRecordBasedInsertBinding(column, prefix);
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column, String parameterName) {
        return column.renderingStrategy().orElse(renderingStrategy)
                .getRecordBasedInsertBinding(column, prefix, parameterName);
    }
}
