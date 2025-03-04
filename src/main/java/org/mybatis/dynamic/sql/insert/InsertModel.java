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
package org.mybatis.dynamic.sql.insert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.InsertRenderer;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.Validator;

public class InsertModel<T> {
    private final SqlTable table;
    private final T row;
    private final List<AbstractColumnMapping> columnMappings;

    private InsertModel(Builder<T> builder) {
        table = Objects.requireNonNull(builder.table);
        row = Objects.requireNonNull(builder.row);
        columnMappings = Objects.requireNonNull(builder.columnMappings);
        Validator.assertNotEmpty(columnMappings, "ERROR.7"); //$NON-NLS-1$
    }

    public Stream<AbstractColumnMapping> columnMappings() {
        return columnMappings.stream();
    }

    public T row() {
        return row;
    }

    public SqlTable table() {
        return table;
    }

    public InsertStatementProvider<T> render(RenderingStrategy renderingStrategy) {
        return InsertRenderer.withInsertModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static <T> Builder<T> withRow(T row) {
        return new Builder<T>().withRow(row);
    }

    public static class Builder<T> {
        private @Nullable SqlTable table;
        private @Nullable T row;
        private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();

        public Builder<T> withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        public Builder<T> withRow(T row) {
            this.row = row;
            return this;
        }

        public Builder<T> withColumnMappings(List<? extends AbstractColumnMapping> columnMappings) {
            this.columnMappings.addAll(columnMappings);
            return this;
        }

        public InsertModel<T> build() {
            return new InsertModel<>(this);
        }
    }
}
