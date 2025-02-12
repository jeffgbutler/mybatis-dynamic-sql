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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Validator;

public class SelectModel extends AbstractSelectModel {
    private final List<QueryExpressionModel> queryExpressions;
    private final @Nullable String forClause;
    private final @Nullable String waitClause;

    private SelectModel(Builder builder) {
        super(builder);
        queryExpressions = Objects.requireNonNull(builder.queryExpressions);
        Validator.assertNotEmpty(queryExpressions, "ERROR.14"); //$NON-NLS-1$
        forClause = builder.forClause;
        waitClause = builder.waitClause;
    }

    public Stream<QueryExpressionModel> queryExpressions() {
        return queryExpressions.stream();
    }

    public Optional<String> forClause() {
        return Optional.ofNullable(forClause);
    }

    public Optional<String> waitClause() {
        return Optional.ofNullable(waitClause);
    }

    public SelectStatementProvider render(RenderingStrategy renderingStrategy) {
        return SelectRenderer.withSelectModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static Builder withQueryExpressions(List<QueryExpressionModel> queryExpressions) {
        return new Builder().withQueryExpressions(queryExpressions);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private final List<QueryExpressionModel> queryExpressions = new ArrayList<>();
        private @Nullable String forClause;
        private @Nullable String waitClause;

        public Builder withQueryExpression(QueryExpressionModel queryExpression) {
            this.queryExpressions.add(queryExpression);
            return this;
        }

        public Builder withQueryExpressions(List<QueryExpressionModel> queryExpressions) {
            this.queryExpressions.addAll(queryExpressions);
            return this;
        }

        public Builder withForClause(@Nullable String forClause) {
            this.forClause = forClause;
            return this;
        }

        public Builder withWaitClause(@Nullable String waitClause) {
            this.waitClause = waitClause;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public SelectModel build() {
            return new SelectModel(this);
        }
    }
}
