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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class FetchFirstTest {

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
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testOffsetAndFetchFirstAfterFrom() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .offset(22)
                    .fetchFirst(3).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::id).isEqualTo(23),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData offset #{parameters.p1} rows fetch first #{parameters.p2} rows only"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 3L),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 22L)
            );
        }
    }

    @Test
    void testFetchFirstOnlyAfterFrom() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .fetchFirst(3).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::id).isEqualTo(1),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData fetch first #{parameters.p1} rows only"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 3L)
            );
        }
    }

    @Test
    void testOffsetAndFetchFirstAfterWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .where(id, isLessThan(50))
                    .and(id, isGreaterThan(22))
                    .offset(22)
                    .fetchFirst(3).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::id).isEqualTo(45),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} and id > #{parameters.p2,jdbcType=INTEGER} offset #{parameters.p3} rows fetch first #{parameters.p4} rows only"),
                    () -> assertThat(selectStatement.getParameters()).contains(entry("p4", 3L), entry("p3", 22L))
            );
        }
    }

    @Test
    void testFetchFirstOnlyAfterWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .where(id, isLessThan(50))
                    .fetchFirst(3).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::id).isEqualTo(1),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} fetch first #{parameters.p2} rows only"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 3L)
            );
        }
    }

    @Test
    void testOffsetAndFetchFirstAfterOrderBy() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id)
                    .offset(22)
                    .fetchFirst(3).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::id).isEqualTo(23),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id offset #{parameters.p1} rows fetch first #{parameters.p2} rows only"),
                    () -> assertThat(selectStatement)
                            .extracting(SelectStatementProvider::getParameters)
                            .extracting("p2", "p1")
                            .containsExactly(3L, 22L)
            );
        }
    }

    @Test
    void testLimitOnlyAfterOrderBy() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id)
                    .fetchFirst(3).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records).first().isNotNull().extracting(AnimalData::id).isEqualTo(1),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id fetch first #{parameters.p1} rows only"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 3L)
            );
        }
    }
}
