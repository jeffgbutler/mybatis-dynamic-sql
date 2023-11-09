/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.select;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.and;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.sql.JDBCType;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProviderAdapter;

class SelectStatementProviderAdapterTest {

    static final SqlTable table = SqlTable.of("foo");
    static final SqlColumn<Date> column1 = table.column("column1", JDBCType.DATE);
    static final SqlColumn<Integer> column2 = table.column("column2", JDBCType.INTEGER);
    static final SqlColumn<String> column3 = table.column("column3", JDBCType.VARCHAR);

    @Test
    void testSimpleCriteria() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1, column2, column3)
                .from(table)
                .where(column1, isEqualTo(d), and(column2, isEqualTo(33)))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .build()
                .render(RenderingStrategies.RAW_JDBC);

        String expectedFullStatement = "select column1, column2, column3 "
                + "from foo "
                + "where (column1 = ? and column2 = ?) or column2 = ? and column2 < ?";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);

        SelectStatementProvider adapter = new SelectStatementProviderAdapter(selectStatement);

        assertThat(adapter.getParameterBindings()).isEqualTo(selectStatement.getParameterBindings());
    }
}
