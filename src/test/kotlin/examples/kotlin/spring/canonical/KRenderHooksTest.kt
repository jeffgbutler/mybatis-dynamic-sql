/*
 *    Copyright 2016-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.kotlin.spring.canonical

import examples.kotlin.spring.canonical.GeneratedAlwaysDynamicSqlSupport.firstName
import examples.kotlin.spring.canonical.GeneratedAlwaysDynamicSqlSupport.fullName
import examples.kotlin.spring.canonical.GeneratedAlwaysDynamicSqlSupport.generatedAlways
import examples.kotlin.spring.canonical.GeneratedAlwaysDynamicSqlSupport.lastName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.person
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.AbstractStatementComposer
import org.mybatis.dynamic.sql.util.kotlin.elements.fragmentAndParameters
import org.mybatis.dynamic.sql.util.kotlin.spring.delete
import org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.insert
import org.mybatis.dynamic.sql.util.kotlin.spring.withKeyHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional

@Suppress("LargeClass")
@SpringJUnitConfig(SpringConfiguration::class)
@Transactional
open class KRenderHooksTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testDeleteRenderHookOneStep() {
        val rows = template.deleteFrom(person) {
            where { id isEqualTo 1 }
            withRenderingHook {
                initialFragment = fragmentAndParameters {
                    withFragment("/* some comment */")
                    withParameter("name", "fred")
                }
            }
        }
        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testDeleteRenderHookTwoStep() {
        val deleteStatement = deleteFrom(person) {
            where { id isEqualTo 1 }
            withRenderingHook {
                fragmentBeforeTable = fragmentAndParameters {
                    withFragment("/* some comment */")
                    withParameter("name", "fred")
                }
            }
        }

        val expected = "delete from /* some comment */ Person where id = :p1"
        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)
        assertThat(deleteStatement.parameters).containsOnly(entry("p1", 1), entry("name", "fred"))

        val rows = template.delete(deleteStatement)
        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testDeleteRenderHookGlobalCopyright() {
        val deleteStatement = deleteFrom(person) {
            where { id isEqualTo 1 }
            withRenderingHook(globalCopyright())
        }

        val expected = "/* global copyright */ delete from Person where id = :p1"
        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)
        assertThat(deleteStatement.parameters).containsOnly(entry("p1", 1))

        val rows = template.delete(deleteStatement)
        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testDeleteRenderHookGlobalCopyrightCompose() {
        val deleteStatement = deleteFrom(person) {
            where { id isEqualTo 1 }
            withRenderingHook {
                fragmentBeforeTable = fragmentAndParameters {
                    withFragment("/* some comment */")
                    withParameter("name", "fred")
                }
                run(globalCopyright())
            }
        }

        val expected = "/* global copyright */ delete from /* some comment */ Person where id = :p1"
        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)
        assertThat(deleteStatement.parameters).containsOnly(entry("p1", 1), entry("name", "fred"))

        val rows = template.delete(deleteStatement)
        assertThat(rows).isEqualTo(1)
    }

    private fun <T : AbstractStatementComposer<T>> globalCopyright(): T.() -> Unit = {
        initialFragment = fragmentAndParameters {
            withFragment("/* global copyright */")
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testInsertOneStep() {
        val keyHolder = GeneratedKeyHolder()
        val row = GeneratedAlwaysRecord(id = 237, firstName = "Sam", lastName = "Smith", fullName = "John Jones")

        val rows = template.withKeyHolder(keyHolder) {
            insert(row) {
                into(generatedAlways)
                map(GeneratedAlwaysDynamicSqlSupport.id) toProperty "id"
                map(firstName) toProperty "firstName"
                map(lastName) toProperty "lastName"
                map(fullName) toProperty "fullName"
                withRenderingHook {
                    fragmentBeforeValues = "OVERRIDING USER VALUE"
                }
            }
        }

        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsOnly(entry("ID", 22), entry("FULL_NAME", "Sam Smith"))
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testInsertTwoStep() {
        val keyHolder = GeneratedKeyHolder()
        val row = GeneratedAlwaysRecord(id = 237, firstName = "Sam", lastName = "Smith", fullName = "John Jones")

        val insertStatement = insert(row) {
            into(generatedAlways)
            map(GeneratedAlwaysDynamicSqlSupport.id) toProperty "id"
            map(firstName) toProperty "firstName"
            map(lastName) toProperty "lastName"
            map(fullName) toProperty "fullName"
            withRenderingHook {
                fragmentBeforeValues = "OVERRIDING USER VALUE"
            }
        }

        val expected = "insert into GeneratedAlways (id, first_name, last_name, full_name) " +
                "OVERRIDING USER VALUE values (:row.id, :row.firstName, :row.lastName, :row.fullName)"

        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.withKeyHolder(keyHolder) {
            insert(insertStatement)
        }

        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsOnly(entry("ID", 22), entry("FULL_NAME", "Sam Smith"))
    }
}
