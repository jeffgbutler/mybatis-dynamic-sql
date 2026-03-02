/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl;

import org.mybatis.dynamic.sql.TableExpression;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.select.join.JoinType;

public abstract class AbstractJoinOperations<T extends AbstractJoinOperations<T>> extends AbstractBooleanOperations<T> {
    private final JoinType joinType;
    private final TableExpression table;

    protected AbstractJoinOperations(JoinType joinType, TableExpression table) {
        this.joinType = joinType;
        this.table = table;
    }

    protected JoinSpecification buildJoinSpecification() {
        return JoinSpecification.withJoinTable(table)
                .withJoinType(joinType)
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build();
    }
}
