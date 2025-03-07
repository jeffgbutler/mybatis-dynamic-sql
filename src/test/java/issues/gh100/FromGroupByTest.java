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
package issues.gh100;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.count;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class FromGroupByTest {

    @Test
    void testFromGroupByB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        builder1.groupBy(StudentDynamicSqlSupport.name);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByLimitB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.limit(3);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " limit #{parameters.p1}";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByLimitB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.limit(3);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " limit #{parameters.p1}";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByLimitB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        var builder3 = builder2.limit(3);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " limit #{parameters.p1}";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.offset(3);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " offset #{parameters.p1} rows";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.offset(3);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " offset #{parameters.p1} rows";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        var builder3 = builder2.offset(3);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " offset #{parameters.p1} rows";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByFetchFirstB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.fetchFirst(2).rowsOnly();

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " fetch first #{parameters.p1} rows only";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByFetchFirstB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.fetchFirst(2).rowsOnly();

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " fetch first #{parameters.p1} rows only";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByFetchFirstB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        var builder3 = builder2.fetchFirst(2).rowsOnly();

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " fetch first #{parameters.p1} rows only";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOrderByB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.orderBy(StudentDynamicSqlSupport.name);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " order by name";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOrderByB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        builder2.orderBy(StudentDynamicSqlSupport.name);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " order by name";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOrderByB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        SelectDSL<SelectModel> builder3 = builder2.orderBy(StudentDynamicSqlSupport.name);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " order by name";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOrderByOffsetB1() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        SelectDSL<SelectModel> builder3 = builder2.orderBy(StudentDynamicSqlSupport.name);

        builder3.offset(2);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " order by name"
                + " offset #{parameters.p1} rows";

        SelectStatementProvider selectStatement = builder1.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOrderByOffsetB2() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        SelectDSL<SelectModel> builder3 = builder2.orderBy(StudentDynamicSqlSupport.name);

        builder3.offset(2);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " order by name"
                + " offset #{parameters.p1} rows";

        SelectStatementProvider selectStatement = builder2.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOrderByOffsetB3() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        SelectDSL<SelectModel> builder3 = builder2.orderBy(StudentDynamicSqlSupport.name);

        builder3.offset(2);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " order by name"
                + " offset #{parameters.p1} rows";

        SelectStatementProvider selectStatement = builder3.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testFromGroupByOrderByOffsetB4() {
        QueryExpressionDSL<SelectModel> builder1 = select(StudentDynamicSqlSupport.name, count())
                .from(StudentDynamicSqlSupport.student);

        QueryExpressionDSL<SelectModel>.GroupByFinisher builder2 = builder1.groupBy(StudentDynamicSqlSupport.name);

        SelectDSL<SelectModel> builder3 = builder2.orderBy(StudentDynamicSqlSupport.name);

        var builder4 = builder3.offset(2);

        String expected = "select name, count(*)"
                + " from student"
                + " group by name"
                + " order by name"
                + " offset #{parameters.p1} rows";

        SelectStatementProvider selectStatement = builder4.build().render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }
}
