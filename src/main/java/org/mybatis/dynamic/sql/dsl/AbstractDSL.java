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
package org.mybatis.dynamic.sql.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.exception.DuplicateTableAliasException;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.SubQuery;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.util.Buildable;

/**
 * Abstract base class for many DSL implementations. Provides common functionality needed in more
 * than one DSL. May provide functionality not needed in ALL DSLs. All methods are protected so they don't
 * leak into the API unnecessarily.
 *
 * <p>This class does not implement any specific interface. That is an intentional choice to allow for flexibility
 * in composing a DSL based on the interfaces that DSL needs to implement. This class is simply a landing ground
 * for common functionality that can be shared across multiple DSL implementations.</p>
 */
public abstract class AbstractDSL {
    protected final List<Supplier<JoinSpecification>> joinSpecificationSuppliers = new ArrayList<>();
    protected final Map<SqlTable, String> tableAliases = new HashMap<>();

    protected Optional<JoinModel> buildJoinModel() {
        if (joinSpecificationSuppliers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(JoinModel.of(joinSpecificationSuppliers.stream()
                .map(Supplier::get)
                .toList()));
    }

    protected void addJoinSpecificationSupplier(Supplier<JoinSpecification> joinSpecificationSupplier) {
        joinSpecificationSuppliers.add(joinSpecificationSupplier);
    }

    protected void addTableAlias(SqlTable table, String tableAlias) {
        if (tableAliases.containsKey(table)) {
            throw new DuplicateTableAliasException(table, tableAlias, tableAliases.get(table));
        }

        tableAliases.put(table, tableAlias);
    }

    protected SubQuery buildSubQuery(Buildable<SelectModel> selectModel) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .build();
    }

    protected SubQuery buildSubQuery(Buildable<SelectModel> selectModel, @Nullable String alias) {
        return new SubQuery.Builder()
                .withSelectModel(selectModel.build())
                .withAlias(alias)
                .build();
    }
}
