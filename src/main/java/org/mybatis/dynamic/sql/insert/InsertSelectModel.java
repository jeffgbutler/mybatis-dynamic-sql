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

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.insert.render.InsertSelectRenderer;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectModel;

public class InsertSelectModel {
    private final SqlTable table;
    private final @Nullable InsertColumnListModel columnList;
    private final SelectModel selectModel;
    private final StatementConfiguration statementConfiguration;

    private InsertSelectModel(Builder builder) {
        table = Objects.requireNonNull(builder.table);
        columnList = builder.columnList;
        selectModel = Objects.requireNonNull(builder.selectModel);
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration);
    }

    public SqlTable table() {
        return table;
    }

    public SelectModel selectModel() {
        return selectModel;
    }

    public Optional<InsertColumnListModel> columnList() {
        return Optional.ofNullable(columnList);
    }

    public StatementConfiguration statementConfiguration() {
        return statementConfiguration;
    }

    public InsertSelectStatementProvider render(RenderingStrategy renderingStrategy) {
        return InsertSelectRenderer.withInsertSelectModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static Builder withTable(SqlTable table) {
        return new Builder().withTable(table);
    }

    public static class Builder {
        private @Nullable SqlTable table;
        private @Nullable InsertColumnListModel columnList;
        private @Nullable SelectModel selectModel;
        private @Nullable StatementConfiguration statementConfiguration;

        public Builder withTable(SqlTable table) {
            this.table = table;
            return this;
        }

        public Builder withColumnList(@Nullable InsertColumnListModel columnList) {
            this.columnList = columnList;
            return this;
        }

        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }

        public Builder withStatementConfiguration(StatementConfiguration statementConfiguration) {
            this.statementConfiguration = statementConfiguration;
            return this;
        }

        public InsertSelectModel build() {
            return new InsertSelectModel(this);
        }
    }
}
