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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.dsl.JoinOperations;
import org.mybatis.dynamic.sql.exception.DuplicateTableAliasException;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.where.AbstractWhereFinisher;
import org.mybatis.dynamic.sql.where.AbstractWhereStarter;

public abstract class AbstractQueryExpressionDSL<W extends AbstractWhereFinisher<?>,
            T extends AbstractQueryExpressionDSL<W, T>>
        implements AbstractWhereStarter<W, T>, JoinOperations<T> {

    private final List<Supplier<JoinSpecification>> joinSpecificationSuppliers = new ArrayList<>();
    private final Map<SqlTable, String> tableAliases = new HashMap<>();

    protected AbstractQueryExpressionDSL() {
    }

    public void addJoinSpecificationSupplier(Supplier<JoinSpecification> joinSpecificationSupplier) {
        joinSpecificationSuppliers.add(joinSpecificationSupplier);
    }

    protected Optional<JoinModel> buildJoinModel() {
        if (joinSpecificationSuppliers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(JoinModel.of(joinSpecificationSuppliers.stream()
                .map(Supplier::get)
                .toList()));
    }

    public void addTableAlias(SqlTable table, String tableAlias) {
        if (tableAliases.containsKey(table)) {
            throw new DuplicateTableAliasException(table, tableAlias, tableAliases.get(table));
        }

        tableAliases.put(table, tableAlias);
    }

    protected Map<SqlTable, String> tableAliases() {
        return Collections.unmodifiableMap(tableAliases);
    }

    protected static SubQuery buildSubQuery(Buildable<SelectModel> selectModel) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .build();
    }

    protected static SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }
}
