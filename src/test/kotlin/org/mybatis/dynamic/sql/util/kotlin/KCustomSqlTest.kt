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

class KCustomSqlTest {
    @Test
    fun testDeleteHintsMainStatement() {
        val deleteStatement = deleteFrom(person) {
            withSqlAfterKeyword("/* after keyword */")
            withSqlAfterStatement("/* after statement */")
            withSqlBeforeStatement("/* before statement */")
            where {
                id isEqualTo 2
            }
        }

        assertThat(deleteStatement.deleteStatement)
            .isEqualTo("/* before statement */ delete /* after keyword */ from Person where person_id = :p1 /* after statement */")
    }
}
