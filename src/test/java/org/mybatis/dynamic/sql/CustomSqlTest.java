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

import static examples.complexquery.PersonDynamicSqlSupport.id;
import static examples.complexquery.PersonDynamicSqlSupport.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
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

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("/* before statement */ delete /* after keyword */ from Person where person_id = :p1 /* after statement */");
    }

    @Test
    void testDeleteHintsWhereClause1() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterKeyword("/* after keyword */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("delete /* after keyword */ from Person where person_id = :p1");
    }

    @Test
    void testDeleteHintsWhereClause2() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterStatement("/* after statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("delete from Person where person_id = :p1 /* after statement */");
    }

    @Test
    void testDeleteHintsWhereClause3() {
        DeleteStatementProvider deleteStatement = deleteFrom(person)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(deleteStatement.getDeleteStatement()).isEqualTo("/* before statement */ delete from Person where person_id = :p1");
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

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("/* before statement */ update /* after keyword */ Person set person_id = :p1 where person_id = :p2 /* after statement */");
    }

    @Test
    void testUpdateWhereClause1() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterKeyword("/* after keyword */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("update /* after keyword */ Person set person_id = :p1 where person_id = :p2");
    }

    @Test
    void testUpdateHintsWhereClause2() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlAfterStatement("/* after statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("update Person set person_id = :p1 where person_id = :p2 /* after statement */");
    }

    @Test
    void testUpdateHintsWhereClause3() {
        UpdateStatementProvider updateStatement = update(person)
                .set(id).equalTo(3)
                .where(id, isEqualTo(2))
                .configureStatement(c -> c.withSqlBeforeStatement("/* before statement */"))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(updateStatement.getUpdateStatement()).isEqualTo("/* before statement */ update Person set person_id = :p1 where person_id = :p2");
    }
}
