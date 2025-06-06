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

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.bodyWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.brainWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Predicates;

class OptionalConditionsWithPredicatesAnimalDataTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    private static final @Nullable Integer NULL_INTEGER = null;

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
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testAllIgnored() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanWhenPresent(NULL_INTEGER))  // the where clause should not render
                    .orderBy(id)
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData order by id"),
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1),
                    () -> assertThat(animals).element(1).isNotNull().extracting(AnimalData::id).isEqualTo(2)
            );
        }
    }

    @Test
    void testIgnoredBetweenRendered() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(3))
                    .and(id, isNotEqualToWhenPresent(NULL_INTEGER))
                    .or(id, isEqualTo(4))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(3),
                    () -> assertThat(animals).element(1).isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testIgnoredInWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanWhenPresent(NULL_INTEGER))
                    .and(id, isEqualTo(3))
                    .or(id, isEqualTo(4))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(3),
                    () -> assertThat(animals).element(1).isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testManyIgnored() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanWhenPresent(NULL_INTEGER), and(id, isGreaterThanOrEqualToWhenPresent(NULL_INTEGER)))
                    .and(id, isEqualToWhenPresent(NULL_INTEGER), or(id, isEqualTo(3), and(id, isLessThanWhenPresent(NULL_INTEGER))))
                    .or(id, isEqualToWhenPresent(4), and(id, isGreaterThanOrEqualToWhenPresent(NULL_INTEGER)))
                    .and(id, isNotEqualToWhenPresent(NULL_INTEGER))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(3),
                    () -> assertThat(animals).element(1).isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testIgnoredInitialWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanWhenPresent(NULL_INTEGER), and(id, isEqualTo(3)))
                    .or(id, isEqualTo(4))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(3),
                    () -> assertThat(animals).element(1).isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testEqualWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(4))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(1),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testEqualWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualToWhenPresent(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testNotEqualWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualTo(4))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <> #{parameters.p1,jdbcType=INTEGER} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(9),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testNotEqualWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualToWhenPresent(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testGreaterThanWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThan(4))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id > #{parameters.p1,jdbcType=INTEGER} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(6),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(5)
            );
        }
    }

    @Test
    void testGreaterThanWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanWhenPresent(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testGreaterThanOrEqualToWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualTo(4))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id >= #{parameters.p1,jdbcType=INTEGER} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(7),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testGreaterThanOrEqualToWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualToWhenPresent(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(4))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanWhenPresent(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanOrEqualToWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualTo(4))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(4),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanOrEqualToWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualToWhenPresent(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsInWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(4, 5, 6))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER},#{parameters.p3,jdbcType=INTEGER}) order by id"),
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testIsInWhenWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isInWhenPresent(3, NULL_INTEGER, 5).map(i -> i + 3))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(6),
                    () -> assertThat(animals).element(1).isNotNull().extracting(AnimalData::id).isEqualTo(8)
            );
        }
    }

    @Test
    void testIsInCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("mouse", "musk shrew"))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testValueStreamTransformer() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInWhenPresent("  Mouse", "  ", null, "", "Musk shrew  ")
                                    .map(String::trim)
                                    .filter(not(String::isEmpty)))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testValueStreamTransformerWithCustomCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, MyInCondition.isIn("  Mouse", "  ", null, "", "Musk shrew  "))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(4)
            );
        }
    }

    @Test
    void testIsInCaseInsensitiveWhenWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitiveWhenPresent())
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotIn(4, 5, 6))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id not in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER},#{parameters.p3,jdbcType=INTEGER}) and id <= #{parameters.p4,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(7),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInWhenWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotInWhenPresent(3, NULL_INTEGER, 5))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id not in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) and id <= #{parameters.p3,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("mouse", "musk shrew"))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) not in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) and id <= #{parameters.p3,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInCaseInsensitiveWhenWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitiveWhenPresent())
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsBetweenWhenWithValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(3).and(6).filter(Predicates.bothPresent()))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(4),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(3)
            );
        }
    }

    @Test
    void testIsBetweenWhenWithFirstMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetweenWhenPresent(NULL_INTEGER).and(6))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsBetweenWhenWithSecondMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetweenWhenPresent(3).and(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsBetweenWhenWithBothMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetweenWhenPresent(NULL_INTEGER).and(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenWithValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(3).and(6).filter(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id not between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER} and id <= #{parameters.p3,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(6),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenWithFirstMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetweenWhenPresent(NULL_INTEGER).and(6))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenWithSecondMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetweenWhenPresent(3).and(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenWithBothMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetweenWhenPresent(NULL_INTEGER).and(NULL_INTEGER))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsLikeWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLike("%mole"))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(6)
            );
        }
    }

    @Test
    void testIsLikeWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeWhenPresent((String) null))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsLikeCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitive("%MoLe"))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(6)
            );
        }
    }

    @Test
    void testIsLikeCaseInsensitiveWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitiveWhenPresent((String) null))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLike("%mole"))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name not like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeWhenPresent((String) null))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitive("%MoLe"))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) not like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeCaseInsensitiveWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitiveWhenPresent((String) null))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals).first().isNotNull().extracting(AnimalData::id).isEqualTo(1)
            );
        }
    }
}
