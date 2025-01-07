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

import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.Utilities;
import org.mybatis.dynamic.sql.where.AbstractWhereFinisher;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

/**
 * DSL for building count queries. Count queries are specializations of select queries. They have joins and where
 * clauses, but not the other parts of a select (group by, order by, etc.) Count queries always return
 * a long. If these restrictions are not acceptable, then use the Select DSL for an unrestricted select statement.
 *
 * @author Jeff Butler
 */
public class CountDSL extends AbstractQueryExpressionDSL<CountDSL.CountWhereBuilder, CountDSL>
        implements Buildable<SelectModel> {

    private @Nullable CountWhereBuilder whereBuilder;
    private final BasicColumn countColumn;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();

    private CountDSL(BasicColumn countColumn, SqlTable table) {
        super(table);
        this.countColumn = Objects.requireNonNull(countColumn);
    }

    @Override
    public CountWhereBuilder where() {
        whereBuilder = Utilities.buildIfNecessary(whereBuilder, CountWhereBuilder::new);
        return whereBuilder;
    }

    @Override
    public SelectModel build() {
        return buildModel();
    }

    @Override
    public CountDSL configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    private SelectModel buildModel() {
        QueryExpressionModel queryExpressionModel = new QueryExpressionModel.Builder()
                .withSelectColumn(countColumn)
                .withTable(table())
                .withTableAliases(tableAliases())
                .withJoinModel(buildJoinModel().orElse(null))
                .withWhereModel(whereBuilder == null ? null : whereBuilder.buildWhereModel())
                .build();

        return new SelectModel.Builder()
                .withQueryExpression(queryExpressionModel)
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    public static CountDSL countFrom(SqlTable table) {
        return new CountDSL(SqlBuilder.count(), table);
    }

    public static FromGatherer count(BasicColumn column) {
        return new FromGatherer(SqlBuilder.count(column));
    }

    public static FromGatherer countDistinct(BasicColumn column) {
        return new FromGatherer(SqlBuilder.countDistinct(column));
    }

    @Override
    protected CountDSL getThis() {
        return this;
    }

    public static class FromGatherer {
        private final BasicColumn column;

        public FromGatherer(BasicColumn column) {
            this.column = column;
        }

        public CountDSL from(SqlTable table) {
            return new CountDSL(column, table);
        }
    }

    public class CountWhereBuilder extends AbstractWhereFinisher<CountWhereBuilder>
            implements Buildable<SelectModel> {
        private CountWhereBuilder() {
            super(CountDSL.this);
        }

        @Override
        public SelectModel build() {
            return CountDSL.this.build();
        }

        @Override
        protected CountWhereBuilder getThis() {
            return this;
        }

        protected EmbeddedWhereModel buildWhereModel() {
            return super.buildModel();
        }
    }
}
