/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.delete;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.CommonBuilder;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.delete.render.DeleteRenderer;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.where.WhereModel;

public class DeleteModel {
    private final SqlTable table;
    private final String tableAlias;
    private final WhereModel whereModel;
    private final Long limit;
    private final OrderByModel orderByModel;
    private final Consumer<DeleteStatementComposer> renderingHook;

    private DeleteModel(Builder builder) {
        table = Objects.requireNonNull(builder.table());
        whereModel = builder.whereModel();
        tableAlias = builder.tableAlias();
        limit = builder.limit();
        orderByModel = builder.orderByModel();
        renderingHook = Objects.requireNonNull(builder.renderingHook);
    }

    public SqlTable table() {
        return table;
    }

    public Optional<String> tableAlias() {
        return Optional.ofNullable(tableAlias);
    }

    public Optional<WhereModel> whereModel() {
        return Optional.ofNullable(whereModel);
    }

    public Optional<Long> limit() {
        return Optional.ofNullable(limit);
    }

    public Optional<OrderByModel> orderByModel() {
        return Optional.ofNullable(orderByModel);
    }

    @NotNull
    public DeleteStatementProvider render(RenderingStrategy renderingStrategy) {
        return DeleteRenderer.withDeleteModel(this)
                .withRenderingStrategy(renderingStrategy)
                .withRenderingHook(renderingHook) // TODO - why a separate method and not just accessed through the model?
                .build()
                .render();
    }

    public static Builder withTable(SqlTable table) {
        return new Builder().withTable(table);
    }

    public static class Builder extends CommonBuilder<Builder> {
        private Consumer<DeleteStatementComposer> renderingHook;

        public Builder withRenderingHook(Consumer<DeleteStatementComposer> renderingHook) {
            this.renderingHook = renderingHook;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public DeleteModel build() {
            return new DeleteModel(this);
        }
    }
}
