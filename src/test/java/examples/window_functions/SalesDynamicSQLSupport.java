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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;

public final class SalesDynamicSQLSupport {
    public static final Sales sales = new Sales();
    public static final SqlColumn<Integer> year = sales.year;
    public static final SqlColumn<String> country = sales.country;
    public static final SqlColumn<String> product = sales.product;
    public static final SqlColumn<Integer> profit = sales.profit;

    public static final class Sales extends SqlTable {
        public final SqlColumn<Integer> year = column("year", JDBCType.INTEGER);
        public final SqlColumn<String> country = column("country", JDBCType.VARCHAR);
        public final SqlColumn<String> product = column("product", JDBCType.VARCHAR);
        public final SqlColumn<Integer> profit = column("profit", JDBCType.INTEGER);

        public Sales() {
            super("sales");
        }
    }
}
