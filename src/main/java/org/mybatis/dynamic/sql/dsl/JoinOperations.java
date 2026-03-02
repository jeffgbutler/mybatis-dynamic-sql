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
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.RenderableCondition;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.SubQuery;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.util.Buildable;

public interface JoinOperations<D extends JoinOperations<D, F>, F extends AbstractJoinOperations<F>> {

    default JoinSpecificationStarter<F> join(SqlTable joinTable) {
        return new JoinSpecificationStarter<>(JoinType.INNER, joinTable, this::buildFinisher);
    }

    default JoinSpecificationStarter<F> join(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return join(joinTable);
    }

    default JoinSpecificationStarter<F> join(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinSpecificationStarter<>(JoinType.INNER, buildSubQuery(joinTable, tableAlias), this::buildFinisher);
    }

    default JoinSpecificationStarter<F> leftJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter<>(JoinType.LEFT, joinTable, this::buildFinisher);
    }

    default JoinSpecificationStarter<F> leftJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return leftJoin(joinTable);
    }

    default JoinSpecificationStarter<F> leftJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinSpecificationStarter<>(JoinType.LEFT, buildSubQuery(joinTable, tableAlias), this::buildFinisher);
    }

    default JoinSpecificationStarter<F> rightJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter<>(JoinType.RIGHT, joinTable, this::buildFinisher);
    }

    default JoinSpecificationStarter<F>rightJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return rightJoin(joinTable);
    }

    default JoinSpecificationStarter<F> rightJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinSpecificationStarter<>(JoinType.RIGHT, buildSubQuery(joinTable, tableAlias), this::buildFinisher);
    }

    default JoinSpecificationStarter<F> fullJoin(SqlTable joinTable) {
        return new JoinSpecificationStarter<>(JoinType.FULL, joinTable, this::buildFinisher);
    }

    default JoinSpecificationStarter<F> fullJoin(SqlTable joinTable, String tableAlias) {
        addTableAlias(joinTable, tableAlias);
        return fullJoin(joinTable);
    }

    default JoinSpecificationStarter<F> fullJoin(Buildable<SelectModel> joinTable, @Nullable String tableAlias) {
        return new JoinSpecificationStarter<>(JoinType.FULL, buildSubQuery(joinTable, tableAlias), this::buildFinisher);
    }

    // these are the "old style" join methods. They are a complete expression of a join specification
    default D join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                   AndOrCriteriaGroup... andJoinCriteria) {
        return join(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   AndOrCriteriaGroup... andJoinCriteria) {
        return join(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D join(SqlTable joinTable, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        join(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D join(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        join(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D join(Buildable<SelectModel> subQuery, @Nullable String tableAlias, SqlCriterion onJoinCriterion,
                   List<AndOrCriteriaGroup> andJoinCriteria) {
        join(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return leftJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria)) ;
    }

    default D leftJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        leftJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D leftJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        leftJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D leftJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        leftJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                        AndOrCriteriaGroup... andJoinCriteria) {
        return rightJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        AndOrCriteriaGroup... andJoinCriteria) {
        return rightJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D rightJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        rightJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D rightJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                        List<AndOrCriteriaGroup> andJoinCriteria) {
        rightJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D rightJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                        SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        rightJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return fullJoin(joinTable, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       AndOrCriteriaGroup... andJoinCriteria) {
        return fullJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(andJoinCriteria));
    }

    default D fullJoin(SqlTable joinTable, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        fullJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D fullJoin(SqlTable joinTable, String tableAlias, SqlCriterion onJoinCriterion,
                       List<AndOrCriteriaGroup> andJoinCriteria) {
        fullJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    default D fullJoin(Buildable<SelectModel> subQuery, @Nullable String tableAlias,
                       SqlCriterion onJoinCriterion, List<AndOrCriteriaGroup> andJoinCriteria) {
        fullJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria);
        return getDsl();
    }

    void addTableAlias(SqlTable table, String tableAlias);

    private SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }

    // this is similar in function to the "where()" method in WhereOperations. Needs a better name
    F buildFinisher(JoinType joinType, TableExpression joinTable);
    // this is an unfortunate leak of a method into the public API. SHould maybe be something like addJoinSpecification
    D getDsl();

    class JoinSpecificationStarter<F extends AbstractJoinOperations<F>> {
        private final TableExpression joinTable;
        private final JoinType joinType;
        private final BiFunction<JoinType, TableExpression, F> finisherBuilder;

        public JoinSpecificationStarter(JoinType joinType, TableExpression joinTable,
                                        BiFunction<JoinType, TableExpression, F> finisherBuilder) {
            this.joinType = joinType;
            this.joinTable = joinTable;
            this.finisherBuilder = finisherBuilder;
        }

        public F on(SqlCriterion sqlCriterion) {
            F f = finisherBuilder.apply(joinType, joinTable);
            f.setInitialCriterion(sqlCriterion);
            return f;
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> joinCondition) {
            F f = finisherBuilder.apply(joinType, joinTable);
            ColumnAndConditionCriterion<T> criterion = ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(joinCondition)
                    .build();

            f.setInitialCriterion(criterion);
            return f;
        }

        public <T> F on(BindableColumn<T> joinColumn, RenderableCondition<T> onJoinCondition,
                        AndOrCriteriaGroup... subCriteria) {
            F f = finisherBuilder.apply(joinType, joinTable);
            ColumnAndConditionCriterion<T> criterion = ColumnAndConditionCriterion.withColumn(joinColumn)
                    .withCondition(onJoinCondition)
                    .withSubCriteria(Arrays.asList(subCriteria))
                    .build();

            f.setInitialCriterion(criterion);
            return f;
        }
    }
}
