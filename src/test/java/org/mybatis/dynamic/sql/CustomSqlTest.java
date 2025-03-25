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
package org.mybatis.dynamic.sql;

import static examples.simple.PersonDynamicSqlSupport.id;
import static examples.simple.PersonDynamicSqlSupport.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertBatch;
import static org.mybatis.dynamic.sql.SqlBuilder.insertMultiple;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import examples.simple.PersonRecord;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

class CustomSqlTest {
    @Test
    void testDeleteHintsMainStatement() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where(id, isEqualTo(2))
                .configureStatement(c ->
                        c.withSqlAfterKeyword("/* after keyword */")
                                .withSqlAfterStatement("/* after statement */")
                                .withSqlBeforeStatement("/* before statement */")
                )
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("/* before statement */ delete /* after keyword */ from Person where id = :p1 /* after statement */");
    }

    @Test
    void testDeleteHintsWhereClause1() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterKeyword("/* after keyword */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("delete /* after keyword */ from Person where id = :p1");
    }

    @Test
    void testDeleteHintsWhereClause2() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterStatement("/* after statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("delete from Person where id = :p1 /* after statement */");
    }

    @Test
    void testDeleteHintsWhereClause3() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("/* before statement */ delete from Person where id = :p1");
    }

    @Test
    void testInsertHints() {
        PersonRecord row = new PersonRecord();

        InsertStatementProvider<PersonRecord> insertStatement = insert(row)
                .into(person)
                .map(id).toProperty("id")
                .configureStatement(c -> c.withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(insertStatement.getInsertStatement()).isEqualTo("/* before statement */ insert into Person (id) values (:row.id)");
    }

    @Test
    void testInsertMultipleHints() {
        PersonRecord row = new PersonRecord();

        MultiRowInsertStatementProvider<PersonRecord> insertStatement = insertMultiple(row)
                .into(person)
                .map(id).toProperty("id")
                .configureStatement(c -> c.withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(insertStatement.getInsertStatement()).isEqualTo("/* before statement */ insert into Person (id) values (:records[0].id)");
    }

    @Test
    void testInsertBatchHints() {
        PersonRecord row = new PersonRecord();

        BatchInsert<PersonRecord> insertStatement = insertBatch(row)
                .into(person)
                .map(id).toProperty("id")
                .configureStatement(c -> c.withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(insertStatement.getInsertStatementSQL()).isEqualTo("/* before statement */ insert into Person (id) values (:row.id)");
    }

    @Test
    void testUpdateHintsMainStatement() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(2))
                .configureStatement(c ->
                        c.withSqlAfterKeyword("/* after keyword */")
                                .withSqlAfterStatement("/* after statement */")
                                .withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("/* before statement */ update /* after keyword */ Person set id = :p1 where id = :p2 /* after statement */");
    }

    @Test
    void testUpdateWhereClause1() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterKeyword("/* after keyword */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("update /* after keyword */ Person set id = :p1 where id = :p2");
    }

    @Test
    void testUpdateHintsWhereClause2() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterStatement("/* after statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("update Person set id = :p1 where id = :p2 /* after statement */");
    }

    @Test
    void testUpdateHintsWhereClause3() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("/* before statement */ update Person set id = :p1 where id = :p2");
    }
}
