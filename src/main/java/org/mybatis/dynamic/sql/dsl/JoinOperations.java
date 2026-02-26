/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.SubQuery;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;

public interface JoinOperations<T extends JoinOperations<T>> {

    default T join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                  AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.INNER, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    default T join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                  AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T join(SqlTable joinTable, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.INNER, andJoinCriteria);
        return getThis();
    }

    default T join(SqlTable joinTable, String tableAlias, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T join(Buildable<SelectModel> subQuery, @Nullable String tableAlias, @Nullable SqlCriterion onJoinCriterion,
                  List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.INNER,
                andJoinCriteria);
        return getThis();
    }

    default T leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.LEFT, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    default T leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T leftJoin(SqlTable joinTable, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.LEFT, andJoinCriteria);
        return getThis();
    }

    default T leftJoin(SqlTable joinTable, String tableAlias, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T leftJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                      @Nullable SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.LEFT,
                andJoinCriteria);
        return getThis();
    }

    default T rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.RIGHT, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    default T rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T rightJoin(SqlTable joinTable, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.RIGHT, andJoinCriteria);
        return getThis();
    }

    default T rightJoin(SqlTable joinTable, String tableAlias, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T rightJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       @Nullable SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.RIGHT,
                andJoinCriteria);
        return getThis();
    }

    default T fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.FULL, Arrays.asList(andJoinCriteria));
        return getThis();
    }

    default T fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                      AndOrCriteriaGroup... andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T fullJoin(SqlTable joinTable, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(joinTable, onJoinCriterion, JoinType.FULL, andJoinCriteria);
        return getThis();
    }

    default T fullJoin(SqlTable joinTable, String tableAlias, @Nullable SqlCriterion onJoinCriterion,
            List<AndOrCriteriaGroup> andJoinCriteria) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable, onJoinCriterion, andJoinCriteria);
    }

    default T fullJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                      @Nullable SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(buildSubQuery(subQuery, tableAlias), onJoinCriterion, JoinType.FULL,
                andJoinCriteria);
        return getThis();
    }

    private void addJoinSpecificationSupplier(TableExpression joinTable, @Nullable SqlCriterion onJoinCriterion,
                                              JoinType joinType, List<AndOrCriteriaGroup> andJoinCriteria) {
        addJoinSpecificationSupplier(() -> new JoinSpecification.Builder()
                .withJoinTable(joinTable)
                .withJoinType(joinType)
                .withInitialCriterion(onJoinCriterion)
                .withSubCriteria(andJoinCriteria).build());
    }

    void addTableAlias(SqlTable table, String tableAlias);

    private SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }

    T getThis();

    void addJoinSpecificationSupplier(Supplier<JoinSpecification> joinSpecificationSupplier);
}
