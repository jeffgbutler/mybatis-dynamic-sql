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
package org.mybatis.dynamic.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertColumnListModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.GroupByModel;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;
import org.mybatis.dynamic.sql.select.render.FetchFirstPagingModelRenderer;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.InternalError;
import org.mybatis.dynamic.sql.util.Messages;

class InvalidSQLTest {

    private static final SqlTable person = new SqlTable("person");
    private static final SqlColumn<Integer> id = person.column("id");

    @Test
    void testInvalidGeneralInsertStatement() {
        GeneralInsertModel.Builder builder = new GeneralInsertModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.6"));
    }

    @Test
    void testInvalidGeneralInsertStatementWhenAllOptionalsAreDropped() {
        GeneralInsertModel model = insertInto(person)
                .set(id).toValueWhenPresent((Integer) null)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage(Messages.getString("ERROR.9"));
    }

    @Test
    void testInvalidInsertStatement() {
        InsertModel.Builder<TestRow> builder = new InsertModel.Builder<TestRow>()
                .withTable(person)
                .withRow(new TestRow());

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.7"));
    }

    @Test
    void testInvalidInsertStatementWhenAllOptionalsAreDropped() {
        TestRow testRow = new TestRow();

        InsertModel<TestRow> model = insert(testRow)
                .into(person)
                .map(id).toPropertyWhenPresent("id", testRow::getId)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage(Messages.getString("ERROR.10"));
    }

    @Test
    void testInvalidMultipleInsertStatementNoRecords() {
        MultiRowInsertModel.Builder<TestRow> builder = new MultiRowInsertModel.Builder<TestRow>()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.20"));
    }

    @Test
    void testInvalidMultipleInsertStatementNoMappings() {
        List<TestRow> records = List.of(new TestRow());

        MultiRowInsertModel.Builder<TestRow> builder = new MultiRowInsertModel.Builder<TestRow>()
                .withRecords(records)
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.8"));
    }

    @Test
    void testInvalidBatchInsertStatementNoRecords() {
        BatchInsertModel.Builder<TestRow> builder = new BatchInsertModel.Builder<TestRow>()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.19"));
    }

    @Test
    void testInvalidBatchInsertStatementNoMappings() {
        List<TestRow> records = List.of(new TestRow());

        BatchInsertModel.Builder<TestRow> builder = new BatchInsertModel.Builder<TestRow>()
                .withRecords(records)
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.5"));
    }

    @Test
    void testInvalidEmptyInsertColumnList() {
        List<SqlColumn<?>> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> InsertColumnListModel.of(list))
                .withMessage(Messages.getString("ERROR.4"));
    }

    @Test
    void testInvalidNullInsertColumnList() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> InsertColumnListModel.of(null));
    }

    @Test
    void testInvalidSelectStatementWithoutQueryExpressions() {
        SelectModel.Builder builder =
                new SelectModel.Builder().withStatementConfiguration(new StatementConfiguration());

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.14"));
    }

    @Test
    void testInvalidSelectStatementWithoutColumnList() {
        QueryExpressionModel.Builder builder = new QueryExpressionModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.13"));
    }

    @Test
    void testInvalidSelectStatementEmptyJoinModel() {
        List<JoinSpecification> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> JoinModel.of(list))
                .withMessage(Messages.getString("ERROR.15"));
    }

    @Test
    void testInvalidSelectStatementNullJoinModel() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> JoinModel.of(null));
    }

    @Test
    void testInvalidSelectStatementJoinSpecification() {
        JoinSpecification.Builder builder = new JoinSpecification.Builder()
                .withJoinTable(person)
                .withJoinType(JoinType.LEFT);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.16"));
    }
    @Test
    void testInvalidSelectStatementWithEmptyOrderByList() {
        List<SortSpecification> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> OrderByModel.of(list))
                .withMessage(Messages.getString("ERROR.12"));
    }

    @Test
    void testInvalidSelectStatementWithEmptyGroupByList() {
        List<BasicColumn> list = Collections.emptyList();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(() -> GroupByModel.of(list))
                .withMessage(Messages.getString("ERROR.11"));
    }

    @Test
    void testInvalidUpdateStatement() {
        UpdateModel.Builder builder = new UpdateModel.Builder()
                .withTable(person);

        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(builder::build)
                .withMessage(Messages.getString("ERROR.17"));
    }

    @Test
    void testInvalidUpdateStatementWhenAllOptionalsAreDropped() {
        UpdateModel model = update(person)
                .set(id).equalToWhenPresent((Integer) null)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> model.render(RenderingStrategies.SPRING_NAMED_PARAMETER))
                .withMessage(Messages.getString("ERROR.18"));
    }

    @Test
    void testMissingMessage() {
        assertThatExceptionOfType(MissingResourceException.class)
                .isThrownBy(() -> Messages.getString("MISSING_MESSAGE"));
    }

    @Test
    void testInvalidPagingModel() {
        Optional<PagingModel> pagingModel = new PagingModel.Builder().withLimit(22L).build();

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withStatementConfiguration(new StatementConfiguration())
                .build();

        assertThat(pagingModel).hasValueSatisfying(pm -> {
            FetchFirstPagingModelRenderer renderer = new FetchFirstPagingModelRenderer(renderingContext, pm);

            assertThatExceptionOfType(InvalidSqlException.class)
                    .isThrownBy(renderer::render)
                    .withMessage(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_13));
        });
    }

    @Test
    void testInvalidValueAlias() {
        BoundValue<Integer> foo = value(1);

        assertThat(foo.alias()).isEmpty();
        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> foo.as("foo"))
                .withMessage(Messages.getString("ERROR.38"));
    }

    @Test
    void testInvalidDoubleForUpdate() {
        var dsl = select(id).from(person).limit(2).forUpdate();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(dsl::forUpdate)
        .withMessage(Messages.getString("ERROR.48"));
    }

    @Test
    void testInvalidDoubleForShare() {
        var dsl = select(id).from(person).offset(2).forShare();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(dsl::forShare)
                .withMessage(Messages.getString("ERROR.48"));
    }

    @Test
    void testInvalidDoubleForKeyShare() {
        var dsl = select(id).from(person).forKeyShare();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(dsl::forKeyShare)
                .withMessage(Messages.getString("ERROR.48"));
    }

    @Test
    void testInvalidDoubleForNoKeyUpdate() {
        var dsl = select(id).from(person).where(id, isEqualTo(1)).forNoKeyUpdate();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(dsl::forNoKeyUpdate)
                .withMessage(Messages.getString("ERROR.48"));
    }

    @Test
    void testInvalidDoubleForNoKeyUpdateAfterJoin() {
        var dsl = select(id).from(person).join(person).on(id, isEqualTo(id)).skipLocked();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(dsl::skipLocked)
                .withMessage(Messages.getString("ERROR.49"));
    }

    @Test
    void testInvalidDoubleForNoKeyUpdateAfterGroupBy() {
        var dsl = select(id).from(person).groupBy(id).nowait();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(dsl::nowait)
                .withMessage(Messages.getString("ERROR.49"));
    }

    @Test
    void testInvalidDoubleForNoKeyUpdateAfterHaving() {
        var dsl = select(id).from(person).groupBy(id).having(id, isEqualTo(2)).nowait();
        assertThatExceptionOfType(InvalidSqlException.class).isThrownBy(dsl::nowait)
                .withMessage(Messages.getString("ERROR.49"));
    }

    static class TestRow {
        private @Nullable Integer id;

        public @Nullable Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}
