/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.render;

import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.exception.DynamicSqlException;
import org.mybatis.dynamic.sql.util.Messages;

public class RawJDBCRenderingStrategy extends RenderingStrategy {
    private static final String RAW_JDBC_MARKER = "?"; //$NON-NLS-1$

    @Override
    public String formatParameterMapKey(AtomicInteger sequence) {
        return Integer.toString(sequence.getAndIncrement());
    }

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return RAW_JDBC_MARKER;
    }

    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return RAW_JDBC_MARKER;
    }

    @Override
    public String getRecordBasedInsertBinding(BindableColumn<?> column, String parameterName) {
        throw new DynamicSqlException(Messages.getString("ERROR.39")); //$NON-NLS-1$
    }

    @Override
    public String getRecordBasedInsertBinding(BindableColumn<?> column, String prefix, String parameterName) {
        throw new DynamicSqlException(Messages.getString("ERROR.39")); //$NON-NLS-1$
    }
}
