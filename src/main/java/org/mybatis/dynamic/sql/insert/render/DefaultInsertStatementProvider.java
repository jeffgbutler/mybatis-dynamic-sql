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

import java.util.Objects;

import org.jspecify.annotations.Nullable;

public class DefaultInsertStatementProvider<T> implements InsertStatementProvider<T> {
    private final String insertStatement;
    private final T row;

    private DefaultInsertStatementProvider(Builder<T> builder) {
        insertStatement = Objects.requireNonNull(builder.insertStatement);
        row = Objects.requireNonNull(builder.row);
    }

    @Override
    public T getRow() {
        return row;
    }

    @Override
    public String getInsertStatement() {
        return insertStatement;
    }

    public static <T> Builder<T> withRow(T row) {
        return new Builder<T>().withRow(row);
    }

    public static class Builder<T> {
        private @Nullable String insertStatement;
        private @Nullable T row;

        public Builder<T> withInsertStatement(String insertStatement) {
            this.insertStatement = insertStatement;
            return this;
        }

        public Builder<T> withRow(T row) {
            this.row = row;
            return this;
        }

        public DefaultInsertStatementProvider<T> build() {
            return new DefaultInsertStatementProvider<>(this);
        }
    }
}
