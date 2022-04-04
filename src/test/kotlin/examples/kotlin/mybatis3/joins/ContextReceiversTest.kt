/*
 *    Copyright 2016-2022 the original author or authors.
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
package examples.kotlin.mybatis3.joins

import examples.kotlin.mybatis3.joins.ItemMasterDynamicSQLSupport.itemId
import examples.kotlin.mybatis3.joins.ItemMasterDynamicSQLSupport.itemMaster
import examples.kotlin.mybatis3.joins.OrderLineDynamicSQLSupport.orderLine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.AbstractSubselectCondition
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaCollector
import org.mybatis.dynamic.sql.util.kotlin.KotlinSubQueryBuilder
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select

/**
 * Demonstrate use of Kotlin's context receivers to add new keywords to the WHERE DSL. This requires Kotlin
 * 1.6.20+. See the notes below on the contextual declaration for details on how to enable this support.
 */
class ContextReceiversTest {
    @Test
    fun testExtensionByContextReceiver() {

        // find items with orders
        val selectStatement = select(itemMaster.itemId) {
            from (itemMaster)
            where {
                itemId matchesAny {
                    select(orderLine.itemId) {
                        from (orderLine)
                    }
                }
            }
            orderBy(itemMaster.itemId)
        }

        val expected = "select item_id from ItemMaster " +
                "where item_id = any (select item_id from OrderLine) " +
                "order by item_id"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }
}

/**
 * Define a new condition that renders an "any" condition. This is to demonstrate adding a new keyword through a
 * contextual extension function.
 */
class MatchesWithSubSelect<T>(selectModelBuilder: Buildable<SelectModel>) :
    AbstractSubselectCondition<T>(selectModelBuilder) {

    override fun renderCondition(columnName: String, renderedSelectStatement: String) =
        "$columnName = any ($renderedSelectStatement)"
}

/**
 * Define a contextual declaration for the new DSL feature (add "matchesAny" as a DSL keyword in a where clause).
 *
 * Note - this is an experimental feature in Kotlin. It is enabled with the -Xcontext-receivers compiler
 * option (see pom.xml).
 *
 * In IntelliJ, you must add -Xcontext-receivers as an additional command line parameter
 * (Project Structure>Project Settings>Modules>Kotlin)
 */
context (GroupingCriteriaCollector)
infix fun  <T> BindableColumn<T>.matchesAny(subQuery: KotlinSubQueryBuilder.() -> Unit) =
    invoke(MatchesWithSubSelect(KotlinSubQueryBuilder().apply(subQuery)))
