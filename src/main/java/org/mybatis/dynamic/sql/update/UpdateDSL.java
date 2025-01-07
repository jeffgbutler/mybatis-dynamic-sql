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
package org.mybatis.dynamic.sql.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ColumnToColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.SelectMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.Utilities;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;
import org.mybatis.dynamic.sql.where.AbstractWhereFinisher;
import org.mybatis.dynamic.sql.where.AbstractWhereStarter;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class UpdateDSL implements AbstractWhereStarter<UpdateDSL.UpdateWhereBuilder, UpdateDSL>,
        Buildable<UpdateModel> {

    private final List<AbstractColumnMapping> columnMappings = new ArrayList<>();
    private final SqlTable table;
    private final @Nullable String tableAlias;
    private @Nullable UpdateWhereBuilder whereBuilder;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private @Nullable Long limit;
    private @Nullable OrderByModel orderByModel;

    private UpdateDSL(SqlTable table, @Nullable String tableAlias) {
        this.table = Objects.requireNonNull(table);
        this.tableAlias = tableAlias;
    }

    public <T> SetClauseFinisher<T> set(SqlColumn<T> column) {
        return new SetClauseFinisher<>(column);
    }

    @Override
    public UpdateWhereBuilder where() {
        whereBuilder = Utilities.buildIfNecessary(whereBuilder, UpdateWhereBuilder::new);
        return whereBuilder;
    }

    public UpdateDSL limit(long limit) {
        return limitWhenPresent(limit);
    }

    public UpdateDSL limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return this;
    }

    public UpdateDSL orderBy(SortSpecification... columns) {
        return orderBy(Arrays.asList(columns));
    }

    public UpdateDSL orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
        return this;
    }

    /**
     * WARNING! Calling this method could result in an update statement that updates
     * all rows in a table.
     *
     * @return the update model
     */
    @Override
    public UpdateModel build() {
        return UpdateModel.withTable(table)
                .withTableAlias(tableAlias)
                .withColumnMappings(columnMappings)
                .withLimit(limit)
                .withOrderByModel(orderByModel)
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    @Override
    public UpdateDSL configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    public static UpdateDSL update(SqlTable table, @Nullable String tableAlias) {
        return new UpdateDSL(table, tableAlias);
    }

    public static UpdateDSL update(SqlTable table) {
        return update(table, null);
    }

    public class SetClauseFinisher<T> {

        private final SqlColumn<T> column;

        public SetClauseFinisher(SqlColumn<T> column) {
            this.column = column;
        }

        public UpdateDSL equalToNull() {
            columnMappings.add(NullMapping.of(column));
            return UpdateDSL.this;
        }

        public UpdateDSL equalToConstant(String constant) {
            columnMappings.add(ConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }

        public UpdateDSL equalToStringConstant(String constant) {
            columnMappings.add(StringConstantMapping.of(column, constant));
            return UpdateDSL.this;
        }

        public UpdateDSL equalTo(T value) {
            return equalTo(() -> value);
        }

        public UpdateDSL equalTo(Supplier<T> valueSupplier) {
            columnMappings.add(ValueMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }

        public UpdateDSL equalTo(Buildable<SelectModel> buildable) {
            columnMappings.add(SelectMapping.of(column, buildable));
            return UpdateDSL.this;
        }

        public UpdateDSL equalTo(BasicColumn rightColumn) {
            columnMappings.add(ColumnToColumnMapping.of(column, rightColumn));
            return UpdateDSL.this;
        }

        public UpdateDSL equalToOrNull(@Nullable T value) {
            return equalToOrNull(() -> value);
        }

        public UpdateDSL equalToOrNull(Supplier<@Nullable T> valueSupplier) {
            columnMappings.add(ValueOrNullMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }

        public UpdateDSL equalToWhenPresent(@Nullable T value) {
            return equalToWhenPresent(() -> value);
        }

        public UpdateDSL equalToWhenPresent(Supplier<@Nullable T> valueSupplier) {
            columnMappings.add(ValueWhenPresentMapping.of(column, valueSupplier));
            return UpdateDSL.this;
        }
    }

    public class UpdateWhereBuilder extends AbstractWhereFinisher<UpdateWhereBuilder>
            implements Buildable<UpdateModel> {

        private UpdateWhereBuilder() {
            super(UpdateDSL.this);
        }

        public UpdateDSL limit(long limit) {
            return limitWhenPresent(limit);
        }

        public UpdateDSL limitWhenPresent(Long limit) {
            return UpdateDSL.this.limitWhenPresent(limit);
        }

        public UpdateDSL orderBy(SortSpecification... columns) {
            return orderBy(Arrays.asList(columns));
        }

        public UpdateDSL orderBy(Collection<? extends SortSpecification> columns) {
            orderByModel = OrderByModel.of(columns);
            return UpdateDSL.this;
        }

        @Override
        public UpdateModel build() {
            return UpdateDSL.this.build();
        }

        @Override
        protected UpdateWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return buildModel();
        }
    }
}
