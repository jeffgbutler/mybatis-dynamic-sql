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
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class Issue100Test {

    @Test
    void testNormalUsage() {
        SelectStatementProvider selectStatement = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid))
                .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
                .union()
                .select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .orderBy(StudentDynamicSqlSupport.id)
                .limit(3)
                .offset(2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " union"
                + " select id, name, idcard"
                + " from student"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild01() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid));

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild02() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid))
        .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild03() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid))
        .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id);

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild04() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid))
        .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .limit(3);

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild05() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid))
        .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .limit(3)
        .offset(2);

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " limit #{parameters.p2}"
                + " offset #{parameters.p3}";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild06() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, equalTo(StudentRegDynamicSqlSupport.studentid))
        .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .offset(2);

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild07() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid))
        .where(StudentDynamicSqlSupport.idcard, equalTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .offset(2)
        .fetchFirst(3).rowsOnly();

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " offset #{parameters.p2} rows"
                + " fetch first #{parameters.p3} rows only";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSuccessiveBuild08() {
        QueryExpressionDSL<SelectModel> builder = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student);

        builder.join(StudentRegDynamicSqlSupport.studentReg)
        .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid))
        .where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"))
        .orderBy(StudentDynamicSqlSupport.id)
        .fetchFirst(3).rowsOnly();

        SelectStatementProvider selectStatement = builder.build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select student.id, student.name, student.idcard"
                + " from student join student_reg on student.id = student_reg.studentId"
                + " where student.idcard = #{parameters.p1}"
                + " order by id"
                + " fetch first #{parameters.p2} rows only";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void test3() {
        QueryExpressionDSL<SelectModel>.JoinSpecificationFinisher on = select(StudentDynamicSqlSupport.id, StudentDynamicSqlSupport.name, StudentDynamicSqlSupport.idcard)
                .from(StudentDynamicSqlSupport.student)
                .join(StudentRegDynamicSqlSupport.studentReg)
                .on(StudentDynamicSqlSupport.id, isEqualTo(StudentRegDynamicSqlSupport.studentid));

        on.where(StudentDynamicSqlSupport.idcard, isEqualTo("fred"));

        SelectStatementProvider selectStatement = on
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement())
            .isEqualTo("select student.id, student.name, student.idcard from student join student_reg on student.id = student_reg.studentId where student.idcard = #{parameters.p1}");
    }
}
