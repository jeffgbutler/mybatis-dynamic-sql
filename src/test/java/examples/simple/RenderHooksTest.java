/*
 *    Copyright 2016-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.simple;

import static examples.simple.PersonDynamicSqlSupport.occupation;
import static examples.simple.PersonDynamicSqlSupport.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.isNull;

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
import org.mybatis.dynamic.sql.AbstractStatementComposer;
import org.mybatis.dynamic.sql.delete.DeleteStatementComposer;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.function.Consumer;

class RenderHooksTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql");
        assert is != null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(PersonMapper.class);
        config.addMapper(PersonWithAddressMapper.class);
        config.addMapper(AddressMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testDeleteHookOneStep() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete( c ->
                    c.withRenderingHook(co -> co.setInitialFragment(
                            FragmentAndParameters.fromFragment("/* some comment */")))
                            .where(occupation, isNull())
            );
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    void testDeleteHookTwoStep() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DeleteStatementProvider deleteStatement = deleteFrom(person)
                    .where(occupation, isNull())
                    .withRenderingHook(c -> c.setFragmentBeforeTable(FragmentAndParameters
                            .withFragment("/* some comment */")
                            .withParameter("name", "fred")
                            .build())
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "delete from /* some comment */ Person where occupation is null";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
            assertThat(deleteStatement.getParameters()).containsExactly(entry("name", "fred"));

            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    void testDeleteHookTwoStepAndThen() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DeleteStatementProvider deleteStatement = deleteFrom(person)
                    .where(occupation, isNull())
                    .withRenderingHook(copyrightHook.andThen(dsc -> dsc.setFragmentBeforeTable(
                            FragmentAndParameters
                                    .withFragment("/* some comment */")
                                    .withParameter("name", "fred")
                                    .build()))
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "/* my copyright */ delete from /* some comment */ Person where occupation is null";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
            assertThat(deleteStatement.getParameters()).containsExactly(entry("name", "fred"));

            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    void testDeleteHookComposeGlobalCopyright() {
        Consumer<DeleteStatementComposer> hook = dsc -> dsc.setFragmentBeforeTable(
                FragmentAndParameters
                        .withFragment("/* some comment */")
                        .withParameter("name", "fred")
                        .build()
                );

        try (SqlSession session = sqlSessionFactory.openSession()) {
            DeleteStatementProvider deleteStatement = deleteFrom(person)
                    .where(occupation, isNull())
                    .withRenderingHook(hook.andThen(globalCopyright()))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "/* global copyright */ delete from /* some comment */ Person where occupation is null";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
            assertThat(deleteStatement.getParameters()).containsExactly(entry("name", "fred"));

            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    void testDeleteHookGlobalCopyright() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DeleteStatementProvider deleteStatement = deleteFrom(person)
                    .where(occupation, isNull())
                    .withRenderingHook(globalCopyright())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "/* global copyright */ delete from Person where occupation is null";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
            assertThat(deleteStatement.getParameters()).isEmpty();

            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(2);
        }
    }

    private <T extends AbstractStatementComposer<T>> Consumer<T> globalCopyright() {
        return c -> c.setInitialFragment(FragmentAndParameters.fromFragment("/* global copyright */"));
    }

    private final Consumer<DeleteStatementComposer> copyrightHook =
            c -> c.setInitialFragment(FragmentAndParameters.fromFragment("/* my copyright */"));
}
