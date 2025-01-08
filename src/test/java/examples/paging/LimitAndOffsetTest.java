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
package examples.paging;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;
import static examples.animal.data.AnimalDataDynamicSqlSupport.bodyWeight;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import examples.animal.data.AnimalData;
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
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class LimitAndOffsetTest {

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
        config.addMapper(LimitAndOffsetMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testLimitAndOffset() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            LimitAndOffsetMapper mapper = sqlSession.getMapper(LimitAndOffsetMapper.class);

            List<AnimalData> rows = mapper.selectWithLimitAndOffset(5, 3, d -> d.orderBy(id));

            assertThat(rows).hasSize(5);
            assertThat(rows.get(0).getId()).isEqualTo(4);
        }
    }

    @Test
    void testLimitAndOffsetDirect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            LimitAndOffsetMapper mapper = sqlSession.getMapper(LimitAndOffsetMapper.class);

            SelectStatementProvider selectStatement = SqlBuilder.select(id, animalName, brainWeight, bodyWeight)
                    .from(animalData)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            SelectStatementProvider decorator = new LimitAndOffsetDecorator(5, 3, selectStatement);

            List<AnimalData> rows = mapper.selectMany(decorator);

            assertThat(rows).hasSize(5);
            assertThat(rows.get(0).getId()).isEqualTo(4);
        }
    }
}
