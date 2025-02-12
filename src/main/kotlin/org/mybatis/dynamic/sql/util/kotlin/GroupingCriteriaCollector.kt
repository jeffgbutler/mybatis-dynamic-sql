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

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.ExistsCriterion
import org.mybatis.dynamic.sql.NotCriterion
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.VisitableCondition

typealias GroupingCriteriaReceiver = GroupingCriteriaCollector.() -> Unit

fun GroupingCriteriaReceiver.andThen(after: SubCriteriaCollector.() -> Unit): GroupingCriteriaReceiver = {
    invoke(this)
    after(this)
}

@MyBatisDslMarker
sealed class SubCriteriaCollector {
    internal val subCriteria = mutableListOf<AndOrCriteriaGroup>()

    /**
     * Add sub criterion joined with "and" to the current context. If the receiver adds more than one
     * criterion that renders then parentheses will be added.
     *
     * This function may be called multiple times in a context.
     *
     * @param criteriaReceiver a function to create the contained criteria
     */
    fun and(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteriaReceiver).let {
            subCriteria.add(
                AndOrCriteriaGroup.Builder().withConnector("and") //$NON-NLS-1$
                    .withInitialCriterion(it.initialCriterion)
                    .withSubCriteria(it.subCriteria)
                    .build()
            )
        }

    /**
     * Add a list of criteria joined with "and" to the current context. If the list contains more than
     * one criterion that renders then parentheses will be added. This function is distinguished from the
     * other overload in that it can accept a pre-created list of criteria and does not require any criterion
     * to be the initial criterion. The first criterion that renders will be rendered without the "and" or "or".
     *
     * This function may be called multiple times in a context.
     *
     * @param criteria a list of pre-created criteria
     *
     */
    fun and(criteria: List<AndOrCriteriaGroup>) {
        subCriteria.add(
            AndOrCriteriaGroup.Builder().withConnector("and") //$NON-NLS-1$
                .withSubCriteria(criteria)
                .build()
        )
    }

    /**
     * Add sub criterion joined with "or" to the current context. If the receiver adds more than one
     * criterion that renders then parentheses will be added.
     *
     * This function may be called multiple times in a context.
     *
     * @param criteriaReceiver a function to create the contained criteria
     */
    fun or(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteriaReceiver).let {
            subCriteria.add(
                AndOrCriteriaGroup.Builder().withConnector("or") //$NON-NLS-1$
                    .withInitialCriterion(it.initialCriterion)
                    .withSubCriteria(it.subCriteria)
                    .build()
            )
        }

    /**
     * Add a list of criteria joined with "or" to the current context. If the list contains more than
     * one criterion that renders then parentheses will be added. This function is distinguished from the
     * other overload in that it can accept a pre-created list of criteria and does not require any criterion
     * to be the initial criterion. The first criterion that renders will be rendered without the "and" or "or".
     *
     * This function may be called multiple times in a context.
     *
     * @param criteria a list of pre-created criteria
     *
     */
    fun or(criteria: List<AndOrCriteriaGroup>) {
        subCriteria.add(
            AndOrCriteriaGroup.Builder().withConnector("or") //$NON-NLS-1$
                .withSubCriteria(criteria)
                .build()
        )
    }
}

/**
 * This class is used to gather criteria for a having or where clause. The class gathers two types of criteria:
 * an initial criterion, and sub-criteria connected by either an "and" or an "or".
 *
 * An initial criterion can be one of four types:
 * - A column and condition (called with the invoke operator on a column, or an infix function)
 * - An exists operator (called with the "exists" function)
 * - A criteria group which is essentially parenthesis within the where clause (called with the "group" function)
 * - A criteria group preceded with "not" (called with the "not" function)
 *
 * Only one of the initial criterion functions should be called within each scope. If you need more than one,
 * use a sub-criterion joined with "and" or "or"
 */
@Suppress("TooManyFunctions")
@MyBatisDslMarker
open class GroupingCriteriaCollector : SubCriteriaCollector() {
    internal var initialCriterion: SqlCriterion? = null
        private set(value) {
            assertNull(field, "ERROR.21") //$NON-NLS-1$
            field = value
        }

    /**
     * Add an initial criterion preceded with "not" to the current context. If the receiver adds more than one
     * criterion that renders then parentheses will be added.
     *
     * This may only be called once per scope, and cannot be combined with "exists", "group", "invoke",
     * or any infix function in the same scope.
     *
     * @param criteriaReceiver a function to create the contained criteria
     */
    fun not(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteriaReceiver).let {
            initialCriterion = NotCriterion.Builder()
                .withInitialCriterion(it.initialCriterion)
                .withSubCriteria(it.subCriteria)
                .build()
        }

    /**
     * Add an initial criterion preceded with "not" to the current context. If the list contains more than
     * one criterion that renders then parentheses will be added. This function is distinguished from the
     * other overload in that it can accept a pre-created list of criteria and does not require any criterion
     * to be the initial criterion. The first criterion that renders will be rendered without the "and" or "or".
     *
     * This may only be called once per scope, and cannot be combined with "exists", "group", "invoke",
     * or any infix function in the same scope.
     *
     * @param criteria a list of pre-created criteria
     *
     */
    fun not(criteria: List<AndOrCriteriaGroup>) {
        initialCriterion = NotCriterion.Builder().withSubCriteria(criteria).build()
    }

    /**
     * Add an initial criterion composed of a sub-query preceded with "exists" to the current context.
     *
     * This should only be specified once per scope, and cannot be combined with "invoke",
     * "group", "not", or any infix function in the same scope.
     *
     * @param kotlinSubQueryBuilder a function to create a select statement
     */
    fun exists(kotlinSubQueryBuilder: KotlinSubQueryBuilder.() -> Unit): Unit =
        KotlinSubQueryBuilder().apply(kotlinSubQueryBuilder).let {
            initialCriterion = ExistsCriterion.Builder().withExistsPredicate(SqlBuilder.exists(it)).build()
        }

    /**
     * Add an initial criterion to the current context. If the receiver adds more than one
     * criterion that renders at runtime then parentheses will be added.
     *
     * This may only be specified once per scope, and cannot be combined with "exists", "invoke",
     * "not", or any infix function in the same scope.
     *
     * This could "almost" be an operator invoke function. The problem is that
     * to call it a user would need to use "this" explicitly. We think that is too
     * confusing, so we'll stick with the function name of "group"
     *
     * @param criteriaReceiver a function to create the contained criteria
     */
    fun group(criteriaReceiver: GroupingCriteriaReceiver): Unit =
        GroupingCriteriaCollector().apply(criteriaReceiver).let {
            initialCriterion = CriteriaGroup.Builder()
                .withInitialCriterion(it.initialCriterion)
                .withSubCriteria(it.subCriteria)
                .build()
        }

    /**
     * Add an initial criterion preceded to the current context. If the list contains more than
     * one criterion that renders then parentheses will be added. This function is distinguished from the
     * other overload in that it can accept a pre-created list of criteria and does not require any criterion
     * to be the initial criterion. The first criterion that renders will be rendered without the "and" or "or".
     *
     * This may only be specified once per scope, and cannot be combined with "exists", "invoke",
     * "not", or any infix function in the same scope.
     *
     * @param criteria a list of pre-created criteria
     *
     */
    fun group(criteria: List<AndOrCriteriaGroup>) {
        initialCriterion = CriteriaGroup.Builder().withSubCriteria(criteria).build()
    }

    /**
     * Add an initial criterion to the current context based on a column and condition.
     * You can use it like "A.invoke(isEqualTo(3))" or "A (isEqualTo(3))".
     *
     * This is an extension function to a BindableColumn, but is scoped to the context of the
     * current collector.
     *
     * This should only be specified once per scope, and cannot be combined with "exists", "group",
     * "not", or any infix function in the same scope.
     *
     * @param condition the condition to be applied to this column, in this scope
     */
    operator fun <T : Any> BindableColumn<T>.invoke(condition: VisitableCondition<T>) {
        initialCriterion = ColumnAndConditionCriterion.withColumn(this)
            .withCondition(condition)
            .build()
    }

    // infix functions...we may be able to rewrite these as extension functions once Kotlin solves the multiple
    // receivers problem (https://youtrack.jetbrains.com/issue/KT-42435)

    // conditions for all data types
    fun BindableColumn<*>.isNull() = invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNull())

    fun BindableColumn<*>.isNotNull() = invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotNull())

    infix fun <T : Any> BindableColumn<T>.isEqualTo(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo(value))

    infix fun BindableColumn<*>.isEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo(subQuery))

    infix fun BindableColumn<*>.isEqualTo(column: BasicColumn) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isEqualToWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isEqualToWhenPresent(value))

    infix fun <T : Any> BindableColumn<T>.isNotEqualTo(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualTo(value))

    infix fun BindableColumn<*>.isNotEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualTo(subQuery))

    infix fun BindableColumn<*>.isNotEqualTo(column: BasicColumn) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isNotEqualToWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualToWhenPresent(value))

    infix fun <T : Any> BindableColumn<T>.isGreaterThan(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThan(value))

    infix fun BindableColumn<*>.isGreaterThan(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThan(subQuery))

    infix fun BindableColumn<*>.isGreaterThan(column: BasicColumn) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThan(column))

    infix fun <T : Any> BindableColumn<T>.isGreaterThanWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanWhenPresent(value))

    infix fun <T : Any> BindableColumn<T>.isGreaterThanOrEqualTo(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanOrEqualTo(value))

    infix fun BindableColumn<*>.isGreaterThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanOrEqualTo(subQuery))

    infix fun BindableColumn<*>.isGreaterThanOrEqualTo(column: BasicColumn) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanOrEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isGreaterThanOrEqualToWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanOrEqualToWhenPresent(value))

    infix fun <T : Any> BindableColumn<T>.isLessThan(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThan(value))

    infix fun BindableColumn<*>.isLessThan(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThan(subQuery))

    infix fun BindableColumn<*>.isLessThan(column: BasicColumn) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThan(column))

    infix fun <T : Any> BindableColumn<T>.isLessThanWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanWhenPresent(value))

    infix fun <T : Any> BindableColumn<T>.isLessThanOrEqualTo(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualTo(value))

    infix fun BindableColumn<*>.isLessThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualTo(subQuery))

    infix fun BindableColumn<*>.isLessThanOrEqualTo(column: BasicColumn) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualTo(column))

    infix fun <T : Any> BindableColumn<T>.isLessThanOrEqualToWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualToWhenPresent(value))

    fun <T : Any> BindableColumn<T>.isIn(vararg values: T) = isIn(values.asList())

    @JvmName("isInArray")
    infix fun <T : Any> BindableColumn<T>.isIn(values: Array<out T>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isIn(values))

    infix fun <T : Any> BindableColumn<T>.isIn(values: Collection<T>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isIn(values))

    infix fun BindableColumn<*>.isIn(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isIn(subQuery))

    fun <T : Any> BindableColumn<T>.isInWhenPresent(vararg values: T?) = isInWhenPresent(values.asList())

    @JvmName("isInArrayWhenPresent")
    infix fun <T : Any> BindableColumn<T>.isInWhenPresent(values: Array<out T?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isInWhenPresent(values))

    infix fun <T : Any> BindableColumn<T>.isInWhenPresent(values: Collection<T?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isInWhenPresent(values))

    fun <T : Any> BindableColumn<T>.isNotIn(vararg values: T) = isNotIn(values.asList())

    @JvmName("isNotInArray")
    infix fun <T : Any> BindableColumn<T>.isNotIn(values: Array<out T>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotIn(values))

    infix fun <T : Any> BindableColumn<T>.isNotIn(values: Collection<T>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotIn(values))

    infix fun BindableColumn<*>.isNotIn(subQuery: KotlinSubQueryBuilder.() -> Unit) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotIn(subQuery))

    fun <T : Any> BindableColumn<T>.isNotInWhenPresent(vararg values: T?) = isNotInWhenPresent(values.asList())

    @JvmName("isNotInArrayWhenPresent")
    infix fun <T : Any> BindableColumn<T>.isNotInWhenPresent(values: Array<out T?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotInWhenPresent(values))

    infix fun <T : Any> BindableColumn<T>.isNotInWhenPresent(values: Collection<T?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotInWhenPresent(values))

    infix fun <T : Any> BindableColumn<T>.isBetween(value1: T) =
        SecondValueCollector<T> {
            invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isBetween(value1).and(it))
        }

    infix fun <T : Any> BindableColumn<T>.isBetweenWhenPresent(value1: T?) =
        NullableSecondValueCollector<T> {
            invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isBetweenWhenPresent(value1).and(it))
        }

    infix fun <T : Any> BindableColumn<T>.isNotBetween(value1: T) =
        SecondValueCollector<T> {
            invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetween(value1).and(it))
        }

    infix fun <T : Any> BindableColumn<T>.isNotBetweenWhenPresent(value1: T?) =
        NullableSecondValueCollector<T> {
            invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetweenWhenPresent(value1).and(it))
        }

    // for string columns, but generic for columns with type handlers
    infix fun <T : Any> BindableColumn<T>.isLike(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLike(value))

    infix fun <T : Any> BindableColumn<T>.isLikeWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLikeWhenPresent(value))

    infix fun <T : Any> BindableColumn<T>.isNotLike(value: T) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotLike(value))

    infix fun <T : Any> BindableColumn<T>.isNotLikeWhenPresent(value: T?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotLikeWhenPresent(value))

    // shortcuts for booleans
    fun BindableColumn<Boolean>.isTrue() = isEqualTo(true)

    fun BindableColumn<Boolean>.isFalse() = isEqualTo(false)

    // conditions for strings only
    infix fun BindableColumn<String>.isLikeCaseInsensitive(value: String) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLikeCaseInsensitive(value))

    infix fun BindableColumn<String>.isLikeCaseInsensitiveWhenPresent(value: String?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isLikeCaseInsensitiveWhenPresent(value))

    infix fun BindableColumn<String>.isNotLikeCaseInsensitive(value: String) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotLikeCaseInsensitive(value))

    infix fun BindableColumn<String>.isNotLikeCaseInsensitiveWhenPresent(value: String?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotLikeCaseInsensitiveWhenPresent(value))

    fun BindableColumn<String>.isInCaseInsensitive(vararg values: String) = isInCaseInsensitive(values.asList())

    @JvmName("isInArrayCaseInsensitive")
    infix fun BindableColumn<String>.isInCaseInsensitive(values: Array<out String>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitive(values))

    infix fun BindableColumn<String>.isInCaseInsensitive(values: Collection<String>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitive(values))

    fun BindableColumn<String>.isInCaseInsensitiveWhenPresent(vararg values: String?) =
        isInCaseInsensitiveWhenPresent(values.asList())

    @JvmName("isInArrayCaseInsensitiveWhenPresent")
    infix fun BindableColumn<String>.isInCaseInsensitiveWhenPresent(values: Array<out String?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitiveWhenPresent(values))

    infix fun BindableColumn<String>.isInCaseInsensitiveWhenPresent(values: Collection<String?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitiveWhenPresent(values))

    fun BindableColumn<String>.isNotInCaseInsensitive(vararg values: String) =
        isNotInCaseInsensitive(values.asList())

    @JvmName("isNotInArrayCaseInsensitive")
    infix fun BindableColumn<String>.isNotInCaseInsensitive(values: Array<out String>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitive(values))

    infix fun BindableColumn<String>.isNotInCaseInsensitive(values: Collection<String>) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitive(values))

    fun BindableColumn<String>.isNotInCaseInsensitiveWhenPresent(vararg values: String?) =
        isNotInCaseInsensitiveWhenPresent(values.asList())

    @JvmName("isNotInArrayCaseInsensitiveWhenPresent")
    infix fun BindableColumn<String>.isNotInCaseInsensitiveWhenPresent(values: Array<out String?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitiveWhenPresent(values))

    infix fun BindableColumn<String>.isNotInCaseInsensitiveWhenPresent(values: Collection<String?>?) =
        invoke(org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitiveWhenPresent(values))

    companion object {
        fun having(receiver: GroupingCriteriaReceiver): GroupingCriteriaReceiver = receiver

        /**
         * Function for code simplification. This allows creation of an independent where clause
         * that can be reused in different statements. For example:
         *
         * val whereClause = where { id isEqualTo 3 }
         *
         * val rows = countFrom(foo) {
         *   where(whereClause)
         * }
         *
         * Use of this function is optional. You can also write code like this:
         *
         * val whereClause: GroupingCriteriaReceiver = { id isEqualTo 3 }
         *
         */
        fun where(receiver: GroupingCriteriaReceiver): GroupingCriteriaReceiver = receiver
    }
}

class SecondValueCollector<T> (private val consumer: (T) -> Unit) {
    infix fun and(value2: T) = consumer.invoke(value2)
}

class NullableSecondValueCollector<T> (private val consumer: (T?) -> Unit) {
    infix fun and(value2: T?) = consumer.invoke(value2)
}
