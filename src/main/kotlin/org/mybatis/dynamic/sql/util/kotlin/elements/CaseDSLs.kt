/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.util.kotlin.elements

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.caseexpression.BasicWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.ConditionBasedWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseWhenCondition
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaCollector
import org.mybatis.dynamic.sql.util.kotlin.assertNull

class KSearchedCaseDSL : KElseDSL {
    internal var elseValue: BasicColumn? = null
        private set(value) {
            assertNull(field, "ERROR.42") //$NON-NLS-1$
            field = value
        }
    internal val whenConditions = mutableListOf<SearchedCaseWhenCondition>()

    fun `when`(dslCompleter: SearchedCaseCriteriaCollector.() -> Unit) =
        SearchedCaseCriteriaCollector().apply(dslCompleter).run {
            whenConditions.add(SearchedCaseWhenCondition.Builder().withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .withThenValue(thenValue)
                .build())

        }

    override fun `else`(column: BasicColumn) {
        this.elseValue = column
    }
}

class SearchedCaseCriteriaCollector : GroupingCriteriaCollector(), KThenDSL {
    internal var thenValue: BasicColumn? = null
        private set(value) {
            assertNull(field, "ERROR.41") //$NON-NLS-1$
            field = value
        }

    override fun then(column: BasicColumn) {
        thenValue = column
    }
}

class KSimpleCaseDSL<T : Any> : KElseDSL {
    internal var elseValue: BasicColumn? = null
        private set(value) {
            assertNull(field, "ERROR.42") //$NON-NLS-1$
            field = value
        }
    internal val whenConditions = mutableListOf<SimpleCaseWhenCondition<T>>()

    fun `when`(firstCondition: VisitableCondition<T>, vararg subsequentConditions: VisitableCondition<T>,
               completer: SimpleCaseThenGatherer.() -> Unit) =
        SimpleCaseThenGatherer().apply(completer).run {
            val allConditions = buildList {
                add(firstCondition)
                addAll(subsequentConditions)
            }

            whenConditions.add(ConditionBasedWhenCondition(allConditions, thenValue))
        }

    fun `when`(firstValue: T, vararg subsequentValues: T, completer: SimpleCaseThenGatherer.() -> Unit) =
        SimpleCaseThenGatherer().apply(completer).run {
            val allConditions = buildList {
                add(firstValue)
                addAll(subsequentValues)
            }

            whenConditions.add(BasicWhenCondition(allConditions, thenValue))
        }

    override fun `else`(column: BasicColumn) {
        this.elseValue = column
    }
}

class SimpleCaseThenGatherer: KThenDSL {
    internal var thenValue: BasicColumn? = null
        private set(value) {
            assertNull(field, "ERROR.41") //$NON-NLS-1$
            field = value
        }

    override fun then(column: BasicColumn) {
        thenValue = column
    }
}

interface KThenDSL {
    fun then(value: String) {
        then(stringConstant(value))
    }

    fun then(value: Boolean) {
        then(constant<String>(value.toString()))
    }

    fun then(value: Int) {
        then(constant<String>(value.toString()))
    }

    fun then(value: Long) {
        then(constant<String>(value.toString()))
    }

    fun then(value: Double) {
        then(constant<String>(value.toString()))
    }

    fun then(column: BasicColumn)
}

interface KElseDSL {
    fun `else`(value: String) {
        `else`(stringConstant(value))
    }

    fun `else`(value: Boolean) {
        `else`(constant<String>(value.toString()))
    }

    fun `else`(value: Int) {
        `else`(constant<String>(value.toString()))
    }

    fun `else`(value: Long) {
        `else`(constant<String>(value.toString()))
    }

    fun `else`(value: Double) {
        `else`(constant<String>(value.toString()))
    }

    fun `else`(column: BasicColumn)
}
