/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.GeneralInsertDSL
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable

typealias GeneralInsertCompleter = @MyBatisDslMarker KotlinGeneralInsertBuilder.() -> Unit

@MyBatisDslMarker
class KotlinGeneralInsertBuilder(private val table: SqlTable) : Buildable<GeneralInsertModel> {

    private val columnMappings = mutableListOf<AbstractColumnMapping>()

    fun <T> set(column: SqlColumn<T>) = GeneralInsertColumnSetCompleter(column) {
        columnMappings.add(it)
    }

    override fun build(): GeneralInsertModel =
        with(GeneralInsertDSL.Builder()) {
            withTable(table)
            withColumnMappings(columnMappings)
            build()
        }.build()
}
