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

import examples.simple.PersonDynamicSqlSupport.id
import examples.simple.PersonDynamicSqlSupport.person
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.spring.*

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
            .isEqualTo("/* before statement */ delete /* after keyword */ from Person where id = :p1 /* after statement */")
    }

    @Test
    fun testInsertHints() {
        data class Row(val id: Int, val firstName: String)

        val insertStatement = insert(Row(3, "Fred")) {
            into(person)
            map(id) toProperty "id"
            configureStatement {
                withSqlAfterKeyword("/* after keyword */")
                withSqlAfterStatement("/* after statement */")
                withSqlBeforeStatement("/* before statement */")
            }
        }

        assertThat(insertStatement.insertStatement)
            .isEqualTo("/* before statement */ insert /* after keyword */ into Person (id) values (:row.id) /* after statement */")
    }

    @Test
    fun testInsertBatchHints() {
        data class Row(val id: Int, val firstName: String)

        val insertStatement = insertBatch(listOf( Row(3, "Fred"))) {
            into(person)
            map(id) toProperty "id"
            configureStatement {
                withSqlAfterKeyword("/* after keyword */")
                withSqlAfterStatement("/* after statement */")
                withSqlBeforeStatement("/* before statement */")
            }
        }

        assertThat(insertStatement.insertStatementSQL)
            .isEqualTo("/* before statement */ insert /* after keyword */ into Person (id) values (:row.id) /* after statement */")
    }

    @Test
    fun testInsertMultipleHints() {
        data class Row(val id: Int, val firstName: String)

        val insertStatement = insertMultiple(listOf( Row(3, "Fred"))) {
            into(person)
            map(id) toProperty "id"
            configureStatement {
                withSqlAfterKeyword("/* after keyword */")
                withSqlAfterStatement("/* after statement */")
                withSqlBeforeStatement("/* before statement */")
            }
        }

        assertThat(insertStatement.insertStatement)
            .isEqualTo("/* before statement */ insert /* after keyword */ into Person (id) values (:records[0].id) /* after statement */")
    }

    @Test
    fun testGeneralInsertHints() {
        val insertStatement= insertInto(person) {
            set(id) toValue 3
            configureStatement {
                withSqlAfterKeyword("/* after keyword */")
                withSqlAfterStatement("/* after statement */")
                withSqlBeforeStatement("/* before statement */")
            }
        }

        assertThat(insertStatement.insertStatement)
            .isEqualTo("/* before statement */ insert /* after keyword */ into Person (id) values (:p1) /* after statement */")
    }

    @Test
    fun testInsertSelectHints() {
        val insertStatement= insertSelect {
            into(person)
            select(person.allColumns()) {
                from(person)
                where { id isGreaterThan 0 }
            }
            configureStatement {
                withSqlAfterKeyword("/* after keyword */")
                withSqlAfterStatement("/* after statement */")
                withSqlBeforeStatement("/* before statement */")
            }
        }

        assertThat(insertStatement.insertStatement)
            .isEqualTo("/* before statement */ insert /* after keyword */ into Person select * from Person where id > :p1 /* after statement */")
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
            .isEqualTo("/* before statement */ update /* after keyword */ Person set id = :p1 where id = :p2 /* after statement */")
    }
}
