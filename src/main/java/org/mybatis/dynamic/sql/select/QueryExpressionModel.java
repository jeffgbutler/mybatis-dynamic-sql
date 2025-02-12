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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class QueryExpressionModel {
    private final @Nullable String connector;
    private final boolean isDistinct;
    private final List<BasicColumn> selectList;
    private final TableExpression table;
    private final @Nullable JoinModel joinModel;
    private final Map<SqlTable, String> tableAliases;
    private final @Nullable EmbeddedWhereModel whereModel;
    private final @Nullable GroupByModel groupByModel;
    private final @Nullable HavingModel havingModel;

    private QueryExpressionModel(Builder builder) {
        connector = builder.connector;
        isDistinct = builder.isDistinct;
        selectList = Objects.requireNonNull(builder.selectList);
        table = Objects.requireNonNull(builder.table);
        joinModel = builder.joinModel;
        tableAliases = builder.tableAliases;
        whereModel = builder.whereModel;
        groupByModel = builder.groupByModel;
        havingModel = builder.havingModel;
        Validator.assertNotEmpty(selectList, "ERROR.13"); //$NON-NLS-1$
    }

    public Optional<String> connector() {
        return Optional.ofNullable(connector);
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    public Stream<BasicColumn> columns() {
        return selectList.stream();
    }

    public TableExpression table() {
        return table;
    }

    public Map<SqlTable, String> tableAliases() {
        return tableAliases;
    }

    public Optional<EmbeddedWhereModel> whereModel() {
        return Optional.ofNullable(whereModel);
    }

    public Optional<JoinModel> joinModel() {
        return Optional.ofNullable(joinModel);
    }

    public Optional<GroupByModel> groupByModel() {
        return Optional.ofNullable(groupByModel);
    }

    public Optional<HavingModel> havingModel() {
        return Optional.ofNullable(havingModel);
    }

    public static Builder withSelectList(List<? extends BasicColumn> columnList) {
        return new Builder().withSelectList(columnList);
    }

    public static class Builder {
        private @Nullable String connector;
        private boolean isDistinct;
        private final List<BasicColumn> selectList = new ArrayList<>();
        private @Nullable TableExpression table;
        private final Map<SqlTable, String> tableAliases = new HashMap<>();
        private @Nullable EmbeddedWhereModel whereModel;
        private @Nullable JoinModel joinModel;
        private @Nullable GroupByModel groupByModel;
        private @Nullable HavingModel havingModel;

        public Builder withConnector(@Nullable String connector) {
            this.connector = connector;
            return this;
        }

        public Builder withTable(TableExpression table) {
            this.table = table;
            return this;
        }

        public Builder isDistinct(boolean isDistinct) {
            this.isDistinct = isDistinct;
            return this;
        }

        public Builder withSelectColumn(BasicColumn selectColumn) {
            this.selectList.add(selectColumn);
            return this;
        }

        public Builder withSelectList(List<? extends BasicColumn> selectList) {
            this.selectList.addAll(selectList);
            return this;
        }

        public Builder withTableAliases(Map<SqlTable, String> tableAliases) {
            this.tableAliases.putAll(tableAliases);
            return this;
        }

        public Builder withWhereModel(@Nullable EmbeddedWhereModel whereModel) {
            this.whereModel = whereModel;
            return this;
        }

        public Builder withJoinModel(@Nullable JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }

        public Builder withGroupByModel(@Nullable GroupByModel groupByModel) {
            this.groupByModel = groupByModel;
            return this;
        }

        public Builder withHavingModel(@Nullable HavingModel havingModel) {
            this.havingModel = havingModel;
            return this;
        }

        public QueryExpressionModel build() {
            return new QueryExpressionModel(this);
        }
    }
}
