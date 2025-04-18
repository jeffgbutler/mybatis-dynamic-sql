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
package examples.kotlin.spring.canonical

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column

object GeneratedAlwaysDynamicSqlSupport {
    val generatedAlways = GeneratedAlways()
    val id = generatedAlways.id
    val firstName = generatedAlways.firstName
    val lastName = generatedAlways.lastName
    val fullName = generatedAlways.fullName

    class GeneratedAlways : SqlTable("GeneratedAlways") {
        val id = column<Int>(name = "id")
        val firstName = column<String>(name = "first_name")
        val lastName = column<String>(name = "last_name")
        val fullName = column<String>(name = "full_name")
    }
}
