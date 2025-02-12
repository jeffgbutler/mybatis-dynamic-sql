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
package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

class AnimalDataTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/animal/data/CreateAnimalData.sql");
        assert is != null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(AnimalDataMapper.class);
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testSelectAllRows() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::getId).isEqualTo(1)
            );
        }
    }

    @Test
    void testSelectAllRowsWithNullLimit() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .limitWhenPresent(null)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::getId).isEqualTo(1)
            );
        }
    }

    @Test
    void testSelectAllRowsWithRowBounds() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            RowBounds rowBounds = new RowBounds(4, 6);

            List<AnimalData> animals = mapper.selectManyWithRowBounds(selectStatement, rowBounds);
            assertAll(
                    () -> assertThat(animals).hasSize(6),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::getId).isEqualTo(5)
            );
        }
    }

    @Test
    void testSelectAllRowsWithOrder() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::getId).isEqualTo(65)
            );
        }
    }

    @Test
    void testSelectAllRowsAllColumns() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id DESC"),
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals).first().isNotNull()
                            .extracting("ID", "ANIMAL_NAME")
                            .containsExactly(65, "Brachiosaurus")
            );
        }
    }

    @Test
    void testSelectAllRowsAllColumnsWithOrder() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id DESC"),
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals).first().isNotNull()
                            .extracting(AnimalData::getId).isEqualTo(65)
            );
        }
    }

    @Test
    void testSelectAllRowsAllColumnsWithOrderAndAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData, "ad")
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select ad.* from AnimalData ad order by id DESC"),
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::getId).isEqualTo(65)
            );
        }
    }

    @Test
    void testSelectRowsLessThan20() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(20))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(19);
        }
    }

    @Test
    void testSelectRowsBetween30And40() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(30).and(40))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(11);
        }
    }

    @Test
    void testSelectRowsNotBetween() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(10).and(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(14);
        }
    }

    @Test
    void testSelectRowsNotBetweenWithNewNot() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(not(id, isBetween(10).and(60)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, animal_name, body_weight, brain_weight"
                    + " from AnimalData"
                    + " where not id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER}";

            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(14);
        }
    }

    @Test
    void testSelectRowsNotBetweenWithNotGroup() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(not(id, isBetween(10).and(60),
                            or(animalName, isEqualTo("Little brown bat"))))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, animal_name, body_weight, brain_weight"
                    + " from AnimalData"
                    + " where not (id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER}"
                    + " or animal_name = #{parameters.p3,jdbcType=VARCHAR})";

            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(13);
        }
    }

    @Test
    void testSelectRowsNotBetweenWithNotAndGroup() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(not(group(id, isBetween(10).and(60),
                            or(animalName, isEqualTo("Little brown bat")))))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, animal_name, body_weight, brain_weight"
                    + " from AnimalData"
                    + " where not (id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER}"
                    + " or animal_name = #{parameters.p3,jdbcType=VARCHAR})";

            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(13);
        }
    }

    @Test
    void testSelectRowsNotBetweenWithStandaloneWhereClause() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            Optional<WhereClauseProvider> whereClause = where(id, isNotBetween(10).and(60))
                    .or(id, isIn(25, 27))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(whereClause).hasValueSatisfying(wc -> {
                List<AnimalData> animals = mapper.selectWithWhereClause(wc);
                assertThat(animals).hasSize(16);
            });
        }
    }

    @Test
    void testComplexConditionWithStandaloneWhereAndTableAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            Optional<WhereClauseProvider> whereClause = where(id, isEqualTo(1), or(bodyWeight, isGreaterThan(1.0)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3, ExplicitTableAliasCalculator.of(animalData, "a"));

            assertThat(whereClause).hasValueSatisfying(wc -> {
                assertThat(wc.getWhereClause()).isEqualTo("where a.id = #{parameters.p1,jdbcType=INTEGER} or a.body_weight > #{parameters.p2,jdbcType=DOUBLE}");
                List<AnimalData> animals = mapper.selectWithWhereClauseAndAlias(wc);
                assertThat(animals).hasSize(59);
            });
        }
    }

    @Test
    void testSelectRowsNotBetweenWithStandaloneWhereClauseLimitAndOffset() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            Optional<WhereClauseProvider> whereClause = where(id, isLessThan(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3, "whereClauseProvider");

            assertThat(whereClause).hasValueSatisfying(wc -> {
                List<AnimalData> animals = mapper.selectWithWhereClauseLimitAndOffset(wc, 5, 15);
                assertThat(animals).hasSize(5);
                assertThat(animals.get(0).getId()).isEqualTo(16);
            });
        }
    }

    @Test
    void testSelectRowsNotBetweenWithStandaloneWhereClauseAliasLimitAndOffset() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            Optional<WhereClauseProvider> whereClause = where(id, isLessThan(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3, ExplicitTableAliasCalculator.of(animalData, "b"),
                            "whereClauseProvider");

            assertThat(whereClause).hasValueSatisfying(wc -> {
                List<AnimalData> animals = mapper.selectWithWhereClauseAliasLimitAndOffset(wc, 3, 24);
                assertThat(animals).hasSize(3);
                assertThat(animals.get(0).getId()).isEqualTo(25);
            });

        }
    }

    @Test
    void testUnionSelectWithWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(20))
                    .union()
                    .select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThan(40))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union "
                    + "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id > #{parameters.p2,jdbcType=INTEGER}";

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(44),
                    () -> assertThat(selectStatement.getParameters()).hasSize(2),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 20),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 40)
            );
        }
    }

    @Test
    void testUnionSelectWithoutWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .union()
                    .selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "union "
                    + "select distinct id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(selectStatement.getParameters()).isEmpty()
            );
        }
    }

    @Test
    void testUnionAllSelectWithoutWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .unionAll()
                    .selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "union all "
                    + "select distinct id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(130),
                    () -> assertThat(selectStatement.getParameters()).isEmpty()
            );
        }
    }

    @Test
    void testUnionSelectWithTableAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(id, isLessThan(20))
                    .union()
                    .select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "b")
                    .where(id, isGreaterThan(40))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, a.body_weight, a.brain_weight "
                    + "from AnimalData a "
                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union "
                    + "select b.id, b.animal_name, b.body_weight, b.brain_weight "
                    + "from AnimalData b "
                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(44),
                    () -> assertThat(selectStatement.getParameters()).hasSize(2),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 20),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 40)
            );
        }
    }

    @Test
    void testUnionAllSelectWithTableAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(id, isLessThan(20))
                    .unionAll()
                    .select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "b")
                    .where(id, isGreaterThan(40))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, a.body_weight, a.brain_weight "
                    + "from AnimalData a "
                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union all "
                    + "select b.id, b.animal_name, b.body_weight, b.brain_weight "
                    + "from AnimalData b "
                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(44),
                    () -> assertThat(selectStatement.getParameters()).hasSize(2),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 20),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 40)
            );
        }
    }

    @Test
    void testUnionSelectWithTableAndColumnAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id.as("animalId"), animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(id, isLessThan(20))
                    .union()
                    .select(id.as("animalId"), animalName, bodyWeight, brainWeight)
                    .from(animalData, "b")
                    .where(id, isGreaterThan(40))
                    .orderBy(sortColumn("animalId"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id as animalId, a.animal_name, a.body_weight, a.brain_weight "
                    + "from AnimalData a "
                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union "
                    + "select b.id as animalId, b.animal_name, b.body_weight, b.brain_weight "
                    + "from AnimalData b "
                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
                    + "order by animalId";

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(44),
                    () -> assertThat(selectStatement.getParameters()).hasSize(2),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 20),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 40)
            );
        }
    }

    @Test
    void testIsEqualCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(5))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(1);
        }
    }

    @Test
    void testIsNotEqualCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualTo(5))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(64);
        }
    }

    @Test
    void testIsGreaterThanOrEqualToCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualTo(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(6);
        }
    }

    @Test
    void testIsLessThanOrEqualToCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualTo(10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(10);
        }
    }

    @Test
    void testInCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(3);
        }
    }

    @Test
    void testInConditionWithEventuallyEmptyList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isInWhenPresent(null, 22, null).filter(i -> i != 22))
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData");
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(65);
        }
    }

    @Test
    void testInConditionWithEventuallyEmptyListForceRendering() {
        List<Integer> inValues = new ArrayList<>();
        inValues.add(null);
        inValues.add(22);
        inValues.add(null);

        SelectModel selectModel = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isInWhenPresent(inValues).filter(Objects::nonNull).filter(i -> i != 22))
                .build();

        assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() ->
                selectModel.render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testInConditionWithEmptyList() {
        SelectModel selectModel = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isInWhenPresent(Collections.emptyList()))
                .build();

        assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() ->
                selectModel.render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testInCaseSensitiveCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("yellow-bellied marmot", "verbet", null))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(2);
        }
    }

    @Test
    void testNotInCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(62);
        }
    }

    @Test
    void testNotInCaseSensitiveCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("yellow-bellied marmot", "verbet"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(63);
        }
    }

    @Test
    void testNotInCaseSensitiveConditionWithNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive((String)null))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).isEmpty();
        }
    }

    @Test
    void testNotInConditionWithEventuallyEmptyList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotInWhenPresent(null, 22, null).filter(i -> i != 22))
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData");
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(65);
        }
    }

    @Test
    void testNotInConditionWithEventuallyEmptyListForceRendering() {
        SelectModel selectModel = select(id, animalName, bodyWeight, brainWeight)
                .from(animalData)
                .where(id, isNotInWhenPresent(null, 22, null)
                        .filter(Objects::nonNull).filter(i -> i != 22))
                .build();

        assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() ->
                selectModel.render(RenderingStrategies.MYBATIS3)
        );
    }

    @Test
    void testLikeCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLike("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(2);
        }
    }

    @Test
    void testLikeCaseInsensitive() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).element(0).isNotNull().extracting(AnimalData::getAnimalName).isEqualTo("Ground squirrel"),
                    () -> assertThat(animals).element(1).isNotNull().extracting(AnimalData::getAnimalName).isEqualTo("Artic ground squirrel")
            );
        }
    }

    @Test
    void testLikeLowerCase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, lower(animalName).as("AnimalName"), bodyWeight, brainWeight)
                    .from(animalData)
                    .where(lower(animalName), isLike("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMALNAME").isEqualTo("ground squirrel"),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMALNAME").isEqualTo("artic ground squirrel")
            );
        }
    }

    @Test
    void testLikeUpperCase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, upper(animalName).as("animalname"), bodyWeight, brainWeight)
                    .from(animalData)
                    .where(upper(animalName), isLike("%SQUIRREL"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).element(0).isNotNull().extracting("ANIMALNAME")
                            .isEqualTo("GROUND SQUIRREL"),
                    () -> assertThat(animals).element(1).isNotNull().extracting("ANIMALNAME")
                            .isEqualTo("ARTIC GROUND SQUIRREL")
            );
        }
    }

    @Test
    void testLength() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, upper(animalName).as("animalname"), bodyWeight, brainWeight)
                    .from(animalData)
                    .where(Length.length(animalName), isGreaterThan(22))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMALNAME").isEqualTo("LESSER SHORT-TAILED SHREW"),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMALNAME").isEqualTo("AFRICAN GIANT POUCHED RAT")
            );
        }
    }

    @Test
    void testNumericConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, constant("3").as("some_number"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, 3 as some_number "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "SOME_NUMBER")
                            .containsExactly("African elephant", 3),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "SOME_NUMBER")
                            .containsExactly("Dipliodocus", 3),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "SOME_NUMBER")
                            .containsExactly("Brachiosaurus", 3)
            );
        }
    }

    @Test
    void testStringConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, stringConstant("fred").as("some_string"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, 'fred' as some_string "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "SOME_STRING")
                            .containsExactly("African elephant", "fred"),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "SOME_STRING")
                            .containsExactly("Dipliodocus", "fred"),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "SOME_STRING")
                            .containsExactly("Brachiosaurus", "fred")
            );
        }
    }

    @Test
    void testAdd() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, add(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight + a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 12366.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 11750.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 87154.5)
            );
        }
    }

    @Test
    void testAddConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, add(bodyWeight, constant("22"), constant("33")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight + 22 + 33) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 5767.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 105.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 209.5)
            );
        }
    }

    @Test
    void testAddConstantWithConstantFirst() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, add(constant("22"), bodyWeight, constant("33")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (22 + a.body_weight + 33) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 5767.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 105.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 209.5)
            );
        }
    }

    @Test
    void testConcat() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, concat(animalName, stringConstant(" - The Legend")).as("display_name"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, concat(a.animal_name, ' - The Legend') as display_name "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("African elephant - The Legend"),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("Dipliodocus - The Legend"),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("Brachiosaurus - The Legend")
            );
        }
    }

    @Test
    void testConcatenate() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, concatenate(animalName, stringConstant(" - The Legend")).as("display_name"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, (a.animal_name || ' - The Legend') as display_name "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("African elephant - The Legend"),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("Dipliodocus - The Legend"),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("Brachiosaurus - The Legend")
            );
        }
    }

    @Test
    void testConcatenateConstantFirst() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, concatenate(stringConstant("Name: "), animalName).as("display_name"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, ('Name: ' || a.animal_name) as display_name "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("Name: African elephant"),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("Name: Dipliodocus"),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("DISPLAY_NAME")
                            .isEqualTo("Name: Brachiosaurus")
            );
        }
    }

    @Test
    void testDivide() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, divide(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight / a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0, as(MAP)).isNotNull()
                            .containsEntry("ANIMAL_NAME", "African elephant")
                            .extracting("CALCULATED_WEIGHT", as(DOUBLE)).isEqualTo(0.858, within(0.001)),
                    () -> assertThat(animals).element(1, as(MAP)).isNotNull()
                            .containsEntry("ANIMAL_NAME", "Dipliodocus")
                            .extracting("CALCULATED_WEIGHT", as(DOUBLE)).isEqualTo(0.004, within(0.001)),
                    () -> assertThat(animals).element(2, as(MAP)).isNotNull()
                            .containsEntry("ANIMAL_NAME", "Brachiosaurus")
                            .extracting("CALCULATED_WEIGHT", as(DOUBLE)).isEqualTo(0.001, within(0.001))
            );
        }
    }

    @Test
    void testDivideConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, divide(bodyWeight, constant("10.0")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight / 10.0) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 571.2),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 5.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 15.45)
            );
        }
    }

    @Test
    void testMultiply() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, multiply(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight * a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 38007648.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 585000.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 13441500.0)
            );
        }
    }

    @Test
    void testMultiplyConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, multiply(bodyWeight, constant("2.0")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight * 2.0) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 11424.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 100.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 309.0)
            );
        }
    }

    @Test
    void testSubtract() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, subtract(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight - a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", -942.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", -11650.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", -86845.5)
            );
        }
    }

    @Test
    void testSubtractConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, subtract(bodyWeight, constant("5.5")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight - 5.5) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 5706.5),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 44.5),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 149.0)
            );
        }
    }

    @Test
    void testGeneralOperator() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, applyOperator("-", bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight - a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", -942.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", -11650.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", -86845.5)
            );
        }
    }

    @Test
    void testComplexExpression() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, add(multiply(bodyWeight, constant("5.5")), subtract(brainWeight, constant("2"))).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, ((a.body_weight * 5.5) + (a.brain_weight - 2)) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";

            List<Map<String, Object>> animals = mapper.selectManyMappedRows(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("African elephant", 38068.0),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Dipliodocus", 11973.0),
                    () -> assertThat(animals).element(2).isNotNull()
                            .extracting("ANIMAL_NAME", "CALCULATED_WEIGHT")
                            .containsExactly("Brachiosaurus", 87847.75)
            );
        }
    }

    @Test
    void testNotLikeCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLike("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(63);
        }
    }

    @Test
    void testNotLikeCaseInsensistveCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(63);
        }
    }

    @Test
    void testDeleteThreeRows() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rowCount = mapper.delete(deleteStatement);
            assertThat(rowCount).isEqualTo(3);
        }
    }

    @Test
    void testComplexDelete() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                    .where(id, isLessThan(10))
                    .or(id, isGreaterThan(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rowCount = mapper.delete(deleteStatement);
            assertThat(rowCount).isEqualTo(14);
        }
    }

    @Test
    void testIsNullCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNull())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).isEmpty();
        }
    }

    @Test
    void testIsNotNullCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotNull())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(65);
        }
    }

    @Test
    void testComplexCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .orderBy(id.descending(), bodyWeight)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals).hasSize(4);
        }
    }

    @Test
    void testUpdate() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData row = new AnimalData();
            row.setBodyWeight(2.6);

            UpdateStatementProvider updateStatement = update(animalData)
                    .set(bodyWeight).equalTo(row.getBodyWeight())
                    .set(animalName).equalToNull()
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(4);
        }
    }

    @Test
    void testUpdateValueOrNullWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            UpdateStatementProvider updateStatement = update(animalData)
                    .set(animalName).equalToOrNull("fred")
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(updateStatement.getUpdateStatement()).isEqualTo(
                    "update AnimalData set animal_name = #{parameters.p1,jdbcType=VARCHAR} where id = #{parameters.p2,jdbcType=INTEGER}");
            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testUpdateValueOrNullWithNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            UpdateStatementProvider updateStatement = update(animalData)
                    .set(animalName).equalToOrNull((String) null)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(updateStatement.getUpdateStatement()).isEqualTo(
                    "update AnimalData set animal_name = null where id = #{parameters.p1,jdbcType=INTEGER}");
            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testInsert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData row = new AnimalData();
            row.setId(100);
            row.setAnimalName("Old Shep");
            row.setBodyWeight(22.5);
            row.setBrainWeight(1.2);

            InsertStatementProvider<AnimalData> insertStatement = insert(row)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toProperty("animalName")
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testInsertNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData row = new AnimalData();
            row.setId(100);
            row.setAnimalName("Old Shep");
            row.setBodyWeight(22.5);
            row.setBrainWeight(1.2);

            InsertStatementProvider<AnimalData> insertStatement = insert(row)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toNull()
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testBulkInsert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = new ArrayList<>();
            AnimalData row = new AnimalData();
            row.setId(100);
            row.setAnimalName("Old Shep");
            row.setBodyWeight(22.5);
            records.add(row);

            row = new AnimalData();
            row.setId(101);
            row.setAnimalName("Old Dan");
            row.setBodyWeight(22.5);
            records.add(row);

            BatchInsert<AnimalData> batchInsert = insertBatch(records)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toNull()
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toConstant("1.2")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            batchInsert.insertStatements().forEach(mapper::insert);
            sqlSession.commit();

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(100, 101))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting(AnimalData::getId, AnimalData::getBrainWeight, AnimalData::getAnimalName)
                            .containsExactly(100, 1.2, null),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting(AnimalData::getId, AnimalData::getBrainWeight, AnimalData::getAnimalName)
                            .containsExactly(101, 1.2, null)
            );
        }
    }

    @Test
    void testBulkInsert2() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = new ArrayList<>();
            AnimalData row = new AnimalData();
            row.setId(100);
            row.setAnimalName("Old Shep");
            row.setBodyWeight(22.5);
            records.add(row);

            row = new AnimalData();
            row.setId(101);
            row.setAnimalName("Old Dan");
            row.setBodyWeight(22.5);
            records.add(row);

            BatchInsert<AnimalData> batchInsert = insertBatch(records)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toStringConstant("Old Fred")
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toConstant("1.2")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            batchInsert.insertStatements().forEach(mapper::insert);
            mapper.flush();

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(100, 101))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).element(0).isNotNull()
                            .extracting(AnimalData::getId, AnimalData::getBrainWeight, AnimalData::getAnimalName)
                            .containsExactly(100, 1.2, "Old Fred"),
                    () -> assertThat(animals).element(1).isNotNull()
                            .extracting(AnimalData::getId, AnimalData::getBrainWeight, AnimalData::getAnimalName)
                            .containsExactly(101, 1.2, "Old Fred")
            );
        }
    }

    @Test
    void testOrderByAndDistinct() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending(), animalName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> rows = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(rows).hasSize(14),
                    () -> assertThat(rows).first().isNotNull().extracting(AnimalData::getId).isEqualTo(65)
            );
        }
    }

    @Test
    void testOrderByWithFullClause() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> rows = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(rows).hasSize(14),
                    () -> assertThat(rows).first().isNotNull().extracting(AnimalData::getId).isEqualTo(65)
            );
        }
    }

    @Test
    void testCount() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(count().as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Long count = mapper.selectOneLong(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(*) as total from AnimalData a"),
                    () -> assertThat(count).isEqualTo(65)
            );
        }
    }

    @Test
    void testCountField() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(count(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Long count = mapper.selectOneLong(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(a.brain_weight) as total from AnimalData a"),
                    () -> assertThat(count).isEqualTo(65)
            );
        }
    }

    @Test
    void testCountNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(count())
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Long count = mapper.selectOneLong(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(*) from AnimalData"),
                    () -> assertThat(count).isEqualTo(65)
            );
        }
    }

    @Test
    void testMax() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(max(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Double max = mapper.selectOneDouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select max(a.brain_weight) as total from AnimalData a"),
                    () -> assertThat(max).isEqualTo(87000.0)
            );
        }
    }

    @Test
    void testMaxNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(max(brainWeight))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Double max = mapper.selectOneDouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select max(brain_weight) from AnimalData"),
                    () -> assertThat(max).isEqualTo(87000.0)
            );
        }
    }

    @Test
    void testMaxSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isEqualTo(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight = (select max(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records).hasSize(1),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::getAnimalName).isEqualTo("Brachiosaurus")
            );
        }
    }

    @Test
    void testMin() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(min(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Double min = mapper.selectOneDouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select min(a.brain_weight) as total from AnimalData a"),
                    () -> assertThat(min).isEqualTo(0.005)
            );
        }
    }

    @Test
    void testMinNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(min(brainWeight))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Double min = mapper.selectOneDouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select min(brain_weight) from AnimalData"),
                    () -> assertThat(min).isEqualTo(0.005)
            );
        }
    }

    @Test
    void testMinSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData, "b")))
                    .orderBy(animalName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <> (select min(b.brain_weight) from AnimalData b) order by animal_name"),
                    () -> assertThat(records).hasSize(64),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::getAnimalName).isEqualTo("African elephant")
            );
        }
    }

    @Test
    void testMinSubselectNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData)))
                    .orderBy(animalName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where brain_weight <> (select min(brain_weight) from AnimalData) order by animal_name"),
                    () -> assertThat(records).hasSize(64),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::getAnimalName).isEqualTo("African elephant")
            );
        }
    }

    @Test
    void testAvg() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(avg(brainWeight).as("average"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Double average = mapper.selectOneDouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select avg(a.brain_weight) as average from AnimalData a"),
                    () -> assertThat(average).isEqualTo(1852.69, within(.01))
            );
        }
    }

    @Test
    void testSum() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(sum(brainWeight).as("total"))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Double total = mapper.selectOneDouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select sum(brain_weight) as total from AnimalData"),
                    () -> assertThat(total).isEqualTo(120424.97, within(.01))
            );
        }
    }

    @Test
    void testLessThanSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isLessThan(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight < (select max(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records).hasSize(64)
            );
        }
    }

    @Test
    void testLessThanOrEqualToSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isLessThanOrEqualTo(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <= (select max(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records).hasSize(65)
            );
        }
    }

    @Test
    void testGreaterThanSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isGreaterThan(select(min(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight > (select min(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records).hasSize(64)
            );
        }
    }

    @Test
    void testGreaterThanOrEqualToSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isGreaterThanOrEqualTo(select(min(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight >= (select min(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records).hasSize(65)
            );
        }
    }

    @Test
    void testInsertSelectWithColumnList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SqlTable animalDataCopy = SqlTable.of("AnimalDataCopy");
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
                    .withColumnList(id, animalName, bodyWeight, brainWeight)
                    .withSelectStatement(select(id, animalName, bodyWeight, brainWeight).from(animalData).where(id, isLessThan(22)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into AnimalDataCopy (id, animal_name, body_weight, brain_weight) "
                    + "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id < #{parameters.p1,jdbcType=INTEGER}";

            int rows = mapper.insertSelect(insertSelectStatement);

            assertAll(
                    () -> assertThat(insertSelectStatement.getInsertStatement()).isEqualTo(expected),
                    () -> assertThat(insertSelectStatement.getParameters()).hasSize(1),
                    () -> assertThat(insertSelectStatement.getParameters()).containsEntry("p1", 22),
                    () -> assertThat(rows).isEqualTo(21)
            );
        }
    }

    @Test
    void testInsertSelectWithoutColumnList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SqlTable animalDataCopy = SqlTable.of("AnimalDataCopy");
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
                    .withSelectStatement(select(id, animalName, bodyWeight, brainWeight).from(animalData).where(id, isLessThan(33)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into AnimalDataCopy "
                    + "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id < #{parameters.p1,jdbcType=INTEGER}";
            int rows = mapper.insertSelect(insertSelectStatement);

            assertAll(
                    () -> assertThat(insertSelectStatement.getInsertStatement()).isEqualTo(expected),
                    () -> assertThat(insertSelectStatement.getParameters()).hasSize(1),
                    () -> assertThat(insertSelectStatement.getParameters()).containsEntry("p1", 33),
                    () -> assertThat(rows).isEqualTo(32)
            );
        }
    }

    @Test
    void testGeneralInsert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            GeneralInsertStatementProvider insertStatement = insertInto(animalData)
                    .set(id).toValue(101)
                    .set(animalName).toStringConstant("Fred")
                    .set(brainWeight).toConstant("2.2")
                    .set(bodyWeight).toValue(4.5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into AnimalData (id, animal_name, brain_weight, body_weight) "
                    + "values (#{parameters.p1,jdbcType=INTEGER}, 'Fred', 2.2, #{parameters.p2,jdbcType=DOUBLE})";

            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
            assertThat(insertStatement.getParameters()).hasSize(2);
            assertThat(insertStatement.getParameters()).containsEntry("p1", 101);
            assertThat(insertStatement.getParameters()).containsEntry("p2", 4.5);

            int rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testGeneralInsertValueOrNullWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            GeneralInsertStatementProvider insertStatement = insertInto(animalData)
                    .set(id).toValue(101)
                    .set(animalName).toValueOrNull("Fred")
                    .set(brainWeight).toConstant("2.2")
                    .set(bodyWeight).toValue(4.5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into AnimalData (id, animal_name, brain_weight, body_weight) "
                    + "values (#{parameters.p1,jdbcType=INTEGER}, #{parameters.p2,jdbcType=VARCHAR}, 2.2, "
                    + "#{parameters.p3,jdbcType=DOUBLE})";

            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
            assertThat(insertStatement.getParameters()).hasSize(3);
            assertThat(insertStatement.getParameters()).containsEntry("p1", 101);
            assertThat(insertStatement.getParameters()).containsEntry("p2", "Fred");
            assertThat(insertStatement.getParameters()).containsEntry("p3", 4.5);

            int rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testGeneralInsertValueOrNullWithNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            GeneralInsertStatementProvider insertStatement = insertInto(animalData)
                    .set(id).toValue(101)
                    .set(animalName).toValueOrNull((String) null)
                    .set(brainWeight).toConstant("2.2")
                    .set(bodyWeight).toValue(4.5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into AnimalData (id, animal_name, brain_weight, body_weight) "
                    + "values (#{parameters.p1,jdbcType=INTEGER}, null, 2.2, "
                    + "#{parameters.p2,jdbcType=DOUBLE})";

            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
            assertThat(insertStatement.getParameters()).hasSize(2);
            assertThat(insertStatement.getParameters()).containsEntry("p1", 101);
            assertThat(insertStatement.getParameters()).containsEntry("p2", 4.5);

            int rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testUpdateWithSelect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            UpdateStatementProvider updateStatement = update(animalData)
                    .set(brainWeight).equalTo(select(avg(brainWeight)).from(animalData).where(brainWeight, isGreaterThan(22.0)))
                    .where(brainWeight, isLessThan(1.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update AnimalData "
                    + "set brain_weight = (select avg(brain_weight) from AnimalData where brain_weight > #{parameters.p1,jdbcType=DOUBLE}) "
                    + "where brain_weight < #{parameters.p2,jdbcType=DOUBLE}";
            int rows = mapper.update(updateStatement);

            assertAll(
                    () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected),
                    () -> assertThat(updateStatement.getParameters()).hasSize(2),
                    () -> assertThat(updateStatement.getParameters()).containsEntry("p1", 22.0),
                    () -> assertThat(updateStatement.getParameters()).containsEntry("p2", 1.0),
                    () -> assertThat(rows).isEqualTo(20)
            );
        }
    }

    @Test
    void testUpdateWithAddAndSubtract() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            UpdateStatementProvider updateStatement = update(animalData)
                    .set(brainWeight).equalTo(add(brainWeight, constant("2")))
                    .set(bodyWeight).equalTo(subtract(bodyWeight, constant("3")))
                    .set(animalName).equalToWhenPresent((String) null)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update AnimalData "
                    + "set brain_weight = (brain_weight + 2), body_weight = (body_weight - 3) "
                    + "where id = #{parameters.p1,jdbcType=INTEGER}";

            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters()).hasSize(1);
            assertThat(updateStatement.getParameters()).containsEntry("p1", 1);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);

            AnimalData row = MyBatis3Utils.selectOne(mapper::selectOne,
                    BasicColumn.columnList(id, bodyWeight, brainWeight),
                    animalData,
                    c -> c.where(id, isEqualTo(1))
            );

            assertThat(row.getBodyWeight()).isEqualTo(-2.86);
            assertThat(row.getBrainWeight()).isEqualTo(2.005);
        }
    }

    @Test
    void testUpdateWithMultiplyAndDivide() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);

            UpdateStatementProvider updateStatement = update(animalData)
                    .set(brainWeight).equalTo(divide(brainWeight, constant("2")))
                    .set(bodyWeight).equalTo(multiply(bodyWeight, constant("3")))
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update AnimalData "
                    + "set brain_weight = (brain_weight / 2), body_weight = (body_weight * 3) "
                    + "where id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters()).hasSize(1);
            assertThat(updateStatement.getParameters()).containsEntry("p1", 1);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);

            AnimalData row = MyBatis3Utils.selectOne(mapper::selectOne,
                    BasicColumn.columnList(id, bodyWeight, brainWeight),
                    animalData,
                    c -> c.where(id, isEqualTo(1))
            );

            assertThat(row.getBodyWeight()).isEqualTo(0.42, within(.001));
            assertThat(row.getBrainWeight()).isEqualTo(.0025);
        }
    }
}
