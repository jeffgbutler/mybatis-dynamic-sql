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
package examples.generated.always.mybatis;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class GeneratedAlwaysDynamicSqlSupport {
    public static final GeneratedAlways generatedAlways = new GeneratedAlways();
    public static final SqlColumn<Integer> id = generatedAlways.id;
    public static final SqlColumn<String> firstName = generatedAlways.firstName;
    public static final SqlColumn<String> lastName = generatedAlways.lastName;
    public static final SqlColumn<Integer> age = generatedAlways.age;
    public static final SqlColumn<String> fullName = generatedAlways.fullName;

    public static final class GeneratedAlways extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);
        public final SqlColumn<String> firstName = column("first_name", JDBCType.VARCHAR);
        public final SqlColumn<String> lastName = column("last_name", JDBCType.VARCHAR);
        public final SqlColumn<Integer> age = column("age", JDBCType.VARCHAR);
        public final SqlColumn<String> fullName = column("full_name", JDBCType.VARCHAR);

        public GeneratedAlways() {
            super("GeneratedAlways");
        }
    }
}
