/*
 *    Copyright 2016-2020 the original author or authors.
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
package examples.array;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;

public class NamesTableDynamicSqlSupport {
    public static final NamesTable namesTable = new NamesTable();
    public static final SqlColumn<Integer> id = namesTable.id;
    public static final SqlColumn<String[]> names = namesTable.names;

    public static final class NamesTable extends SqlTable {
        public NamesTable() {
            super("NamesTable");
        }
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);
        public final SqlColumn<String[]> names = column("names", JDBCType.ARRAY,
                "examples.array.StringArrayTypeHandler");
    }
}
