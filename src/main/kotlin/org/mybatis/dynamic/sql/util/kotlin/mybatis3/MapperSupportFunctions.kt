/*
 *    Copyright 2016-2021 the original author or authors.
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
@file:Suppress("TooManyFunctions")
package org.mybatis.dynamic.sql.util.kotlin.mybatis3

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
import org.mybatis.dynamic.sql.util.kotlin.BatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.MultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.elements.insert
import org.mybatis.dynamic.sql.util.kotlin.elements.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.elements.insertMultiple

fun count(
    mapper: (SelectStatementProvider) -> Long,
    column: BasicColumn,
    table: SqlTable,
    completer: CountCompleter
): Long =
    count(column) {
        from(table)
        completer()
    }.run(mapper)

fun countDistinct(
    mapper: (SelectStatementProvider) -> Long,
    column: BasicColumn,
    table: SqlTable,
    completer: CountCompleter
): Long =
    countDistinct(column) {
        from(table)
        completer()
    }.run(mapper)

fun countFrom(mapper: (SelectStatementProvider) -> Long, table: SqlTable, completer: CountCompleter): Long =
    countFrom(table, completer).run(mapper)

fun deleteFrom(mapper: (DeleteStatementProvider) -> Int, table: SqlTable, completer: DeleteCompleter): Int =
    deleteFrom(table, completer).run(mapper)

fun <T> insert(
    mapper: (InsertStatementProvider<T>) -> Int,
    row: T,
    table: SqlTable,
    completer: InsertCompleter<T>
): Int =
    insert(row).into(table, completer).run(mapper)

fun <T> BatchInsert<T>.execute(mapper: (InsertStatementProvider<T>) -> Int): List<Int> =
    insertStatements().map(mapper)

/**
 * This function simply inserts all rows using the supplied mapper. It is up
 * to the user to manage MyBatis3 batch processing externally. When executed with a SqlSession
 * in batch mode, the return value will not contain relevant update counts (each entry in the
 * list will be [org.apache.ibatis.executor.BatchExecutor.BATCH_UPDATE_RETURN_VALUE]).
 * To retrieve update counts, execute [org.apache.ibatis.session.SqlSession.flushStatements].
 */
fun <T> insertBatch(
    mapper: (InsertStatementProvider<T>) -> Int,
    records: Collection<T>,
    table: SqlTable,
    completer: BatchInsertCompleter<T>
): List<Int> =
    insertBatch(records).into(table, completer).execute(mapper)

fun insertInto(
    mapper: (GeneralInsertStatementProvider) -> Int,
    table: SqlTable,
    completer: GeneralInsertCompleter
): Int =
    insertInto(table, completer).run(mapper)

fun <T> insertMultiple(
    mapper: (MultiRowInsertStatementProvider<T>) -> Int,
    records: Collection<T>,
    table: SqlTable,
    completer: MultiRowInsertCompleter<T>
): Int =
    insertMultiple(records).into(table, completer).run(mapper)

fun <T> insertMultipleWithGeneratedKeys(
    mapper: (String, List<T>) -> Int,
    records: Collection<T>,
    table: SqlTable,
    completer: MultiRowInsertCompleter<T>
): Int =
    insertMultiple(records).into(table, completer).run {
        mapper(insertStatement, this.records)
    }

fun insertSelect(
    mapper: (InsertSelectStatementProvider) -> Int,
    table: SqlTable,
    completer: InsertSelectCompleter
): Int =
    insertSelect(table, completer).run(mapper)

fun <T> selectDistinct(
    mapper: (SelectStatementProvider) -> List<T>,
    selectList: List<BasicColumn>,
    table: SqlTable,
    completer: SelectCompleter
): List<T> =
    selectDistinct(selectList) {
        from(table)
        completer()
    }.run(mapper)

fun <T> selectList(
    mapper: (SelectStatementProvider) -> List<T>,
    selectList: List<BasicColumn>,
    table: SqlTable,
    completer: SelectCompleter
): List<T> =
    select(selectList) {
        from(table)
        completer()
    }.run(mapper)

fun <T> selectOne(
    mapper: (SelectStatementProvider) -> T?,
    selectList: List<BasicColumn>,
    table: SqlTable,
    completer: SelectCompleter
): T? =
    select(selectList) {
        from(table)
        completer()
    }.run(mapper)

fun update(mapper: (UpdateStatementProvider) -> Int, table: SqlTable, completer: UpdateCompleter): Int =
    update(table, completer).run(mapper)
