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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.FromGatherer;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.ConfigurableStatement;
import org.mybatis.dynamic.sql.util.Validator;

/**
 * Implements a SQL DSL for building select statements.
 *
 * @author Jeff Butler
 *
 */
public class SelectDSL implements Buildable<SelectModel>, ConfigurableStatement<SelectDSL> {

    private final List<QueryExpressionDSL> queryExpressions = new ArrayList<>();
    private @Nullable OrderByModel orderByModel;
    private @Nullable Long limit;
    private @Nullable Long offset;
    private @Nullable Long fetchFirstRows;
    final StatementConfiguration statementConfiguration = new StatementConfiguration();
    private @Nullable String forClause;
    private @Nullable String waitClause;

    private SelectDSL() {}

    public static QueryExpressionDSL.FromGatherer select(BasicColumn... selectList) {
        return select(Arrays.asList(selectList));
    }

    public static QueryExpressionDSL.FromGatherer select(Collection<? extends BasicColumn> selectList) {
        return new FromGatherer.Builder()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL())
                .build();
    }

    public static QueryExpressionDSL.FromGatherer selectDistinct(BasicColumn... selectList) {
        return selectDistinct(Arrays.asList(selectList));
    }

    public static QueryExpressionDSL.FromGatherer selectDistinct(Collection<? extends BasicColumn> selectList) {
        return new FromGatherer.Builder()
                .withSelectList(selectList)
                .withSelectDSL(new SelectDSL())
                .isDistinct()
                .build();
    }

    void registerQueryExpression(QueryExpressionDSL queryExpression) {
        queryExpressions.add(queryExpression);
    }

    void orderBy(Collection<? extends SortSpecification> columns) {
        orderByModel = OrderByModel.of(columns);
    }

    public LimitFinisher limit(long limit) {
        return limitWhenPresent(limit);
    }

    public LimitFinisher limitWhenPresent(@Nullable Long limit) {
        this.limit = limit;
        return new LimitFinisher();
    }

    public OffsetFirstFinisher offset(long offset) {
        return offsetWhenPresent(offset);
    }

    public OffsetFirstFinisher offsetWhenPresent(@Nullable Long offset) {
        this.offset = offset;
        return new OffsetFirstFinisher();
    }

    public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
        return fetchFirstWhenPresent(fetchFirstRows);
    }

    public FetchFirstFinisher fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
        this.fetchFirstRows = fetchFirstRows;
        return new FetchFirstFinisher();
    }

    public SelectDSL forUpdate() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for update"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL forNoKeyUpdate() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for no key update"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL forShare() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for share"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL forKeyShare() {
        Validator.assertNull(forClause, "ERROR.48"); //$NON-NLS-1$
        forClause = "for key share"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL skipLocked() {
        Validator.assertNull(waitClause, "ERROR.49"); //$NON-NLS-1$
        waitClause = "skip locked"; //$NON-NLS-1$
        return this;
    }

    public SelectDSL nowait() {
        Validator.assertNull(waitClause, "ERROR.49"); //$NON-NLS-1$
        waitClause = "nowait"; //$NON-NLS-1$
        return this;
    }

    @Override
    public SelectDSL configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    @Override
    public SelectModel build() {
        return SelectModel.withQueryExpressions(buildModels())
                .withOrderByModel(orderByModel)
                .withPagingModel(buildPagingModel().orElse(null))
                .withStatementConfiguration(statementConfiguration)
                .withForClause(forClause)
                .withWaitClause(waitClause)
                .build();
    }

    private List<QueryExpressionModel> buildModels() {
        return queryExpressions.stream()
                .map(QueryExpressionDSL::buildModel)
                .toList();
    }

    private Optional<PagingModel> buildPagingModel() {
        return new PagingModel.Builder()
                .withLimit(limit)
                .withOffset(offset)
                .withFetchFirstRows(fetchFirstRows)
                .build();
    }

    public class OffsetFirstFinisher implements SelectDSLForAndWaitOperations, Buildable<SelectModel> {
        public FetchFirstFinisher fetchFirst(long fetchFirstRows) {
            return fetchFirstWhenPresent(fetchFirstRows);
        }

        public FetchFirstFinisher fetchFirstWhenPresent(@Nullable Long fetchFirstRows) {
            SelectDSL.this.fetchFirstRows = fetchFirstRows;
            return new FetchFirstFinisher();
        }

        @Override
        public SelectDSL getSelectDSL() {
            return SelectDSL.this;
        }

        @Override
        public SelectModel build() {
            return SelectDSL.this.build();
        }
    }

    public class LimitFinisher implements SelectDSLForAndWaitOperations, Buildable<SelectModel> {
        public SelectDSL offset(long offset) {
            return offsetWhenPresent(offset);
        }

        public SelectDSL offsetWhenPresent(@Nullable Long offset) {
            SelectDSL.this.offset = offset;
            return SelectDSL.this;
        }

        @Override
        public SelectDSL getSelectDSL() {
            return SelectDSL.this;
        }

        @Override
        public SelectModel build() {
            return SelectDSL.this.build();
        }
    }

    public class FetchFirstFinisher {
        public SelectDSL rowsOnly() {
            return SelectDSL.this;
        }
    }
}
