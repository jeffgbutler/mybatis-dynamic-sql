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
package org.mybatis.dynamic.sql.util.kotlin

import examples.complexquery.PersonDynamicSqlSupport.id
import examples.complexquery.PersonDynamicSqlSupport.person
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.insert
import org.mybatis.dynamic.sql.util.kotlin.spring.insertInto
import org.mybatis.dynamic.sql.util.kotlin.spring.update

class KCustomSqlTest {
    @Test
    fun testDeleteHints() {
        val deleteStatement = deleteFrom(person) {
            where {
                id isEqualTo 2
            }
            configureStatement {
                withSqlAfterKeyword("/* after keyword */")
                withSqlAfterStatement("/* after statement */")
                withSqlBeforeStatement("/* before statement */")
            }
        }

        assertThat(deleteStatement.deleteStatement)
            .isEqualTo("/* before statement */ delete /* after keyword */ from Person where person_id = :p1 /* after statement */")
    }

    @Test
    fun testInsertHints() {
        data class Row(val id: Int, val firstName: String)

        val insertStatement = insert(Row(3, "Fred")) {
            into(person)
            map(id) toProperty "id"
//            withSqlAfterKeyword("/* after keyword */")
//            withSqlAfterStatement("/* after statement */")
//            withSqlBeforeStatement("/* before statement */")
        }

        assertThat(insertStatement.insertStatement)
            .isEqualTo("/* before statement */ insert /* after keyword */ into Person (person_id) values (:id) /* after statement */")
    }
    @Test
    fun testGeneralInsertHints() {
        val insertStatement= insertInto(person) {
//            withSqlAfterKeyword("/* after keyword */")
//            withSqlAfterStatement("/* after statement */")
//            withSqlBeforeStatement("/* before statement */")
            set(id) toValue 3
        }

        assertThat(insertStatement.insertStatement)
            .isEqualTo("/* before statement */ insert /* after keyword */ into Person (person_id) values (:p1) /* after statement */")
    }

    @Test
    fun testUpdateHints() {
        val updateStatement = update(person) {
            set(id) equalTo 3
            where {
                id isEqualTo 2
            }
            configureStatement {
                withSqlAfterKeyword("/* after keyword */")
                withSqlAfterStatement("/* after statement */")
                withSqlBeforeStatement("/* before statement */")
            }
        }

        assertThat(updateStatement.updateStatement)
            .isEqualTo("/* before statement */ update /* after keyword */ Person set person_id = :p1 where person_id = :p2 /* after statement */")
    }
}
