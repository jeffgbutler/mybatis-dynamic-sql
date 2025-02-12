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
package org.mybatis.dynamic.sql.select;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;

public abstract class AbstractSelectModel {
    private final @Nullable OrderByModel orderByModel;
    private final @Nullable PagingModel pagingModel;
    protected final StatementConfiguration statementConfiguration;

    protected AbstractSelectModel(AbstractBuilder<?> builder) {
        orderByModel = builder.orderByModel;
        pagingModel = builder.pagingModel;
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration);
    }

    public Optional<OrderByModel> orderByModel() {
        return Optional.ofNullable(orderByModel);
    }

    public Optional<PagingModel> pagingModel() {
        return Optional.ofNullable(pagingModel);
    }

    public StatementConfiguration statementConfiguration() {
        return statementConfiguration;
    }

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private @Nullable OrderByModel orderByModel;
        private @Nullable PagingModel pagingModel;
        private @Nullable StatementConfiguration statementConfiguration;

        public T withOrderByModel(@Nullable OrderByModel orderByModel) {
            this.orderByModel = orderByModel;
            return getThis();
        }

        public T withPagingModel(@Nullable PagingModel pagingModel) {
            this.pagingModel = pagingModel;
            return getThis();
        }

        public T withStatementConfiguration(StatementConfiguration statementConfiguration) {
            this.statementConfiguration = statementConfiguration;
            return getThis();
        }

        protected abstract T getThis();
    }
}
