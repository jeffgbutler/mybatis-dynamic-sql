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
 * Demonstrate use of Kotlin's context receivers to add new conditions to the WHERE DSL.
 *
 * In IntelliJ this requires use of an early access version of the Kotlin plugin (1.6.20+).
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

// define a new condition that renders an "any" condition
class MatchesWithSubSelect<T>(selectModelBuilder: Buildable<SelectModel>) :
    AbstractSubselectCondition<T>(selectModelBuilder) {

    override fun renderCondition(columnName: String, renderedSelectStatement: String) =
        "$columnName = any ($renderedSelectStatement)"
}

// add the new condition to the DSL with a context receiver (Kotlin 1.6.20+)
context (GroupingCriteriaCollector)
infix fun  <T> BindableColumn<T>.matchesAny(subQuery: KotlinSubQueryBuilder.() -> Unit) =
    invoke(MatchesWithSubSelect(KotlinSubQueryBuilder().apply(subQuery)))
