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
package examples.window_functions;

import static examples.window_functions.SalesDynamicSQLSupport.country;
import static examples.window_functions.SalesDynamicSQLSupport.product;
import static examples.window_functions.SalesDynamicSQLSupport.profit;
import static examples.window_functions.SalesDynamicSQLSupport.sales;
import static examples.window_functions.SalesDynamicSQLSupport.year;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.sum;

import config.TestContainersConfiguration;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.aggregate.WindowDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Testcontainers
class WindowFunctionsTest {

    @SuppressWarnings("resource")
    @Container
    private static final MySQLContainer<?> mysql =
            new MySQLContainer<>(TestContainersConfiguration.MYSQL_LATEST)
                    .withInitScript("examples/window_functions/CreateDB.sql");

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() {
        UnpooledDataSource ds = new UnpooledDataSource(mysql.getDriverClassName(), mysql.getJdbcUrl(),
                mysql.getUsername(), mysql.getPassword());
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void smokeTest() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(year, country, product, profit)
                    .from(sales)
                    .orderBy(country, year, product)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
             List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
             assertThat(rows).hasSize(13);
        }
    }

    @Test
    void testTotalSum() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(sum(profit).as("total_profit"))
                    .from(sales)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(1);
            assertThat(rows.get(0).get("total_profit")).isEqualTo(new BigDecimal(7535));
        }
    }

    @Test
    void testSumByCountry() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(country, sum(profit).as("country_profit"))
                    .from(sales)
                    .groupBy(country)
                    .orderBy(country)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(3);
            assertThat(rows.get(0).get("country_profit")).isEqualTo(new BigDecimal(1610));
            assertThat(rows.get(1).get("country_profit")).isEqualTo(new BigDecimal(1350));
            assertThat(rows.get(2).get("country_profit")).isEqualTo(new BigDecimal(4575));
        }
    }

    @Test
    void testTotalProfitAndCountryProfit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(year, country, product, profit,
                    sum(profit).over().as("total_profit"),
                    sum(profit).over(WindowDSL.partitionBy(country)).as("country_profit"))
                    .from(sales)
                    .orderBy(country, year, product, profit)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(13);
            assertThat(rows.get(0)).containsOnly(
                    entry("year", 2000),
                    entry("country", "Finland"),
                    entry("product", "Computer"),
                    entry("profit", 1500),
                    entry("total_profit", new BigDecimal(7535)),
                    entry("country_profit", new BigDecimal(1610))
            );
            assertThat(rows.get(12)).containsOnly(
                    entry("year", 2001),
                    entry("country", "USA"),
                    entry("product", "TV"),
                    entry("profit", 150),
                    entry("total_profit", new BigDecimal(7535)),
                    entry("country_profit", new BigDecimal(4575))
            );
        }
    }
}
