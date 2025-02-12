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
@file:Suppress("TooManyFunctions")
package org.mybatis.dynamic.sql.util.kotlin.spring

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.render.BatchInsert
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinBatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinMultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.MyBatisDslMarker
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.spring.BatchInsertUtility
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.KeyHolder
import java.sql.ResultSet
import kotlin.reflect.KClass

fun NamedParameterJdbcTemplate.count(selectStatement: SelectStatementProvider): Long =
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, Long::class.java)!!

fun NamedParameterJdbcTemplate.count(column: BasicColumn, completer: CountCompleter): Long =
    count(org.mybatis.dynamic.sql.util.kotlin.spring.count(column, completer))

fun NamedParameterJdbcTemplate.countDistinct(column: BasicColumn, completer: CountCompleter): Long =
    count(org.mybatis.dynamic.sql.util.kotlin.spring.countDistinct(column, completer))

fun NamedParameterJdbcTemplate.countFrom(table: SqlTable, completer: CountCompleter): Long =
    count(org.mybatis.dynamic.sql.util.kotlin.spring.countFrom(table, completer))

fun NamedParameterJdbcTemplate.delete(deleteStatement: DeleteStatementProvider): Int =
    update(deleteStatement.deleteStatement, deleteStatement.parameters)

fun NamedParameterJdbcTemplate.deleteFrom(table: SqlTable, completer: DeleteCompleter): Int =
    delete(org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom(table, completer))

// batch insert
fun <T : Any> NamedParameterJdbcTemplate.insertBatch(insertStatement: BatchInsert<T>): IntArray =
    batchUpdate(insertStatement.insertStatementSQL, BatchInsertUtility.createBatch(insertStatement.records))

fun <T : Any> NamedParameterJdbcTemplate.insertBatch(
    vararg records: T,
    completer: KotlinBatchInsertCompleter<T>
): IntArray =
    insertBatch(records.asList(), completer)

fun <T : Any> NamedParameterJdbcTemplate.insertBatch(
    records: List<T>,
    completer: KotlinBatchInsertCompleter<T>
): IntArray =
    insertBatch(org.mybatis.dynamic.sql.util.kotlin.spring.insertBatch(records, completer))

// single row insert
fun <T : Any> NamedParameterJdbcTemplate.insert(insertStatement: InsertStatementProvider<T>): Int =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement))

fun <T : Any> NamedParameterJdbcTemplate.insert(
    insertStatement: InsertStatementProvider<T>,
    keyHolder: KeyHolder
): Int =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement), keyHolder)

fun <T : Any> NamedParameterJdbcTemplate.insert(row: T, completer: KotlinInsertCompleter<T>): Int =
    insert(org.mybatis.dynamic.sql.util.kotlin.spring.insert(row, completer))

// general insert
fun NamedParameterJdbcTemplate.generalInsert(insertStatement: GeneralInsertStatementProvider): Int =
    update(insertStatement.insertStatement, insertStatement.parameters)

fun NamedParameterJdbcTemplate.generalInsert(
    insertStatement: GeneralInsertStatementProvider,
    keyHolder: KeyHolder
): Int =
    update(insertStatement.insertStatement, MapSqlParameterSource(insertStatement.parameters), keyHolder)

fun NamedParameterJdbcTemplate.insertInto(table: SqlTable, completer: GeneralInsertCompleter): Int =
    generalInsert(org.mybatis.dynamic.sql.util.kotlin.spring.insertInto(table, completer))

// multiple row insert
fun <T : Any> NamedParameterJdbcTemplate.insertMultiple(
    vararg records: T,
    completer: KotlinMultiRowInsertCompleter<T>
): Int =
    insertMultiple(records.asList(), completer)

fun <T : Any> NamedParameterJdbcTemplate.insertMultiple(
    records: List<T>,
    completer: KotlinMultiRowInsertCompleter<T>
): Int =
    insertMultiple(org.mybatis.dynamic.sql.util.kotlin.spring.insertMultiple(records, completer))

fun <T : Any> NamedParameterJdbcTemplate.insertMultiple(insertStatement: MultiRowInsertStatementProvider<T>): Int =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement))

fun <T : Any> NamedParameterJdbcTemplate.insertMultiple(
    insertStatement: MultiRowInsertStatementProvider<T>,
    keyHolder: KeyHolder
): Int =
    update(insertStatement.insertStatement, BeanPropertySqlParameterSource(insertStatement), keyHolder)

fun NamedParameterJdbcTemplate.insertSelect(completer: InsertSelectCompleter): Int =
    insertSelect(org.mybatis.dynamic.sql.util.kotlin.spring.insertSelect(completer))

fun NamedParameterJdbcTemplate.insertSelect(insertStatement: InsertSelectStatementProvider): Int =
    update(insertStatement.insertStatement, MapSqlParameterSource(insertStatement.parameters))

fun NamedParameterJdbcTemplate.insertSelect(
    insertStatement: InsertSelectStatementProvider,
    keyHolder: KeyHolder
): Int =
    update(insertStatement.insertStatement, MapSqlParameterSource(insertStatement.parameters), keyHolder)

// insert with KeyHolder support
fun NamedParameterJdbcTemplate.withKeyHolder(keyHolder: KeyHolder, block: KeyHolderHelper.() -> Int): Int =
    KeyHolderHelper(keyHolder, this).run(block)

fun NamedParameterJdbcTemplate.select(
    vararg selectList: BasicColumn,
    completer: SelectCompleter
): SelectListMapperGatherer =
    select(selectList.toList(), completer)

fun NamedParameterJdbcTemplate.select(
    selectList: List<BasicColumn>,
    completer: SelectCompleter
): SelectListMapperGatherer =
    SelectListMapperGatherer(org.mybatis.dynamic.sql.util.kotlin.spring.select(selectList, completer), this)

fun NamedParameterJdbcTemplate.selectDistinct(
    vararg selectList: BasicColumn,
    completer: SelectCompleter
): SelectListMapperGatherer =
    selectDistinct(selectList.toList(), completer)

fun NamedParameterJdbcTemplate.selectDistinct(
    selectList: List<BasicColumn>,
    completer: SelectCompleter
): SelectListMapperGatherer =
    SelectListMapperGatherer(
        org.mybatis.dynamic.sql.util.kotlin.spring.selectDistinct(selectList, completer),
        this
    )

fun <T> NamedParameterJdbcTemplate.selectList(
    selectStatement: SelectStatementProvider,
    rowMapper: (rs: ResultSet, rowNum: Int) -> T
): List<T> = selectList(selectStatement, RowMapper(rowMapper))

fun <T> NamedParameterJdbcTemplate.selectList(
    selectStatement: SelectStatementProvider,
    rowMapper: RowMapper<T>
): List<T> =
    query(selectStatement.selectStatement, selectStatement.parameters, rowMapper)

fun <T : Any> NamedParameterJdbcTemplate.selectList(
    selectStatement: SelectStatementProvider,
    type: KClass<T>
): List<T> =
    queryForList(selectStatement.selectStatement, selectStatement.parameters, type.java)

fun NamedParameterJdbcTemplate.selectOne(
    vararg selectList: BasicColumn,
    completer: SelectCompleter
): SelectOneMapperGatherer =
    selectOne(selectList.toList(), completer)

fun NamedParameterJdbcTemplate.selectOne(
    selectList: List<BasicColumn>,
    completer: SelectCompleter
): SelectOneMapperGatherer =
    SelectOneMapperGatherer(
        org.mybatis.dynamic.sql.util.kotlin.spring.select(selectList, completer),
        this
    )

fun <T> NamedParameterJdbcTemplate.selectOne(
    selectStatement: SelectStatementProvider,
    rowMapper: (rs: ResultSet, rowNum: Int) -> T
): T? = selectOne(selectStatement, RowMapper(rowMapper))

@SuppressWarnings("SwallowedException")
fun <T> NamedParameterJdbcTemplate.selectOne(
    selectStatement: SelectStatementProvider,
    rowMapper: RowMapper<T>
): T? = try {
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, rowMapper)
} catch (e: EmptyResultDataAccessException) {
    null
}

@SuppressWarnings("SwallowedException")
fun <T : Any> NamedParameterJdbcTemplate.selectOne(
    selectStatement: SelectStatementProvider,
    type: KClass<T>
): T? = try {
    queryForObject(selectStatement.selectStatement, selectStatement.parameters, type.java)
} catch (e: EmptyResultDataAccessException) {
    null
}

fun NamedParameterJdbcTemplate.update(updateStatement: UpdateStatementProvider): Int =
    update(updateStatement.updateStatement, updateStatement.parameters)

fun NamedParameterJdbcTemplate.update(table: SqlTable, completer: UpdateCompleter): Int =
    update(org.mybatis.dynamic.sql.util.kotlin.spring.update(table, completer))

// support classes for select DSL
@MyBatisDslMarker
class SelectListMapperGatherer(
    private val selectStatement: SelectStatementProvider,
    private val template: NamedParameterJdbcTemplate
) {
    fun <T> withRowMapper(rowMapper: (rs: ResultSet, rowNum: Int) -> T): List<T> =
        template.selectList(selectStatement, rowMapper)

    fun <T> withRowMapper(rowMapper: RowMapper<T>): List<T> =
        template.selectList(selectStatement, rowMapper)
}

@MyBatisDslMarker
class SelectOneMapperGatherer(
    private val selectStatement: SelectStatementProvider,
    private val template: NamedParameterJdbcTemplate
) {
    fun <T> withRowMapper(rowMapper: (rs: ResultSet, rowNum: Int) -> T): T? =
        template.selectOne(selectStatement, rowMapper)

    fun <T> withRowMapper(rowMapper: RowMapper<T>): T? =
        template.selectOne(selectStatement, rowMapper)
}

@MyBatisDslMarker
class KeyHolderHelper(private val keyHolder: KeyHolder, private val template: NamedParameterJdbcTemplate) {
    fun insertInto(table: SqlTable, completer: GeneralInsertCompleter): Int =
        template.generalInsert(org.mybatis.dynamic.sql.util.kotlin.spring.insertInto(table, completer), keyHolder)

    fun <T : Any> insert(row: T, completer: KotlinInsertCompleter<T>): Int =
        template.insert(org.mybatis.dynamic.sql.util.kotlin.spring.insert(row, completer), keyHolder)

    fun <T : Any> insertMultiple(vararg records: T, completer: KotlinMultiRowInsertCompleter<T>): Int =
        insertMultiple(records.asList(), completer)

    fun <T : Any> insertMultiple(records: List<T>, completer: KotlinMultiRowInsertCompleter<T>): Int =
        template.insertMultiple(org.mybatis.dynamic.sql.util.kotlin.spring.insertMultiple(records, completer),
            keyHolder)

    fun insertSelect(completer: InsertSelectCompleter): Int =
        template.insertSelect(org.mybatis.dynamic.sql.util.kotlin.spring.insertSelect(completer), keyHolder)
}
