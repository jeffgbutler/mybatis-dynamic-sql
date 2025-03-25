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
package org.mybatis.dynamic.sql.common;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

/**
 * Common model attributes shared by DeleteModel and UpdateModel.
 *
 */
public abstract class CommonModel {
    private final SqlTable table;
    private final @Nullable String tableAlias;
    private final @Nullable EmbeddedWhereModel whereModel;
    private final @Nullable Long limit;
    private final @Nullable OrderByModel orderByModel;
    private final StatementConfiguration statementConfiguration;

    protected CommonModel(CommonBuilder<?> builder) {
        table = Objects.requireNonNull(builder.table());
        whereModel = builder.whereModel();
        tableAlias = builder.tableAlias();
        limit = builder.limit();
        orderByModel = builder.orderByModel();
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration());
    }

    public SqlTable table() {
        return table;
    }

    public Optional<String> tableAlias() {
        return Optional.ofNullable(tableAlias);
    }

    public Optional<EmbeddedWhereModel> whereModel() {
        return Optional.ofNullable(whereModel);
    }

    public Optional<Long> limit() {
        return Optional.ofNullable(limit);
    }

    public Optional<OrderByModel> orderByModel() {
        return Optional.ofNullable(orderByModel);
    }

    public StatementConfiguration statementConfiguration() {
        return statementConfiguration;
    }
}
