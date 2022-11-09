/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.common.OrderByRenderer;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class SelectRenderer {
    private final SelectModel selectModel;
    private final RenderingStrategy renderingStrategy;
    private final AtomicInteger sequence;
    private final TableAliasCalculator parentTableAliasCalculator; // may be null

    private SelectRenderer(Builder builder) {
        selectModel = Objects.requireNonNull(builder.selectModel);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        if (builder.sequence == null) {
            sequence = new AtomicInteger(1);
        } else {
            sequence = builder.sequence;
        }
        parentTableAliasCalculator = builder.parentTableAliasCalculator;
    }

    public SelectStatementProvider render() {
        FragmentCollector fragmentCollector = selectModel
                .mapQueryExpressions(this::renderQueryExpression)
                .collect(FragmentCollector.collect());

        renderOrderBy().ifPresent(fragmentCollector::add);
        renderPagingModel().ifPresent(fragmentCollector::add);

        return toSelectStatementProvider(fragmentCollector);
    }

    private SelectStatementProvider toSelectStatementProvider(FragmentCollector fragmentCollector) {
        return DefaultSelectStatementProvider
                .withSelectStatement(fragmentCollector.fragments().collect(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private FragmentAndParameters renderQueryExpression(QueryExpressionModel queryExpressionModel) {
        return QueryExpressionRenderer.withQueryExpression(queryExpressionModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withParentTableAliasCalculator(parentTableAliasCalculator)
                .build()
                .render();
    }

    private Optional<FragmentAndParameters> renderOrderBy() {
        return selectModel.orderByModel().map(this::renderOrderBy);
    }

    private FragmentAndParameters renderOrderBy(OrderByModel orderByModel) {
        return new OrderByRenderer().render(orderByModel);
    }

    private Optional<FragmentAndParameters> renderPagingModel() {
        return selectModel.pagingModel().map(this::renderPagingModel);
    }

    private FragmentAndParameters renderPagingModel(PagingModel pagingModel) {
        return new PagingModelRenderer.Builder()
                .withPagingModel(pagingModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();
    }

    public static Builder withSelectModel(SelectModel selectModel) {
        return new Builder().withSelectModel(selectModel);
    }

    public static class Builder extends AbstractQueryRendererBuilder<Builder> {
        private SelectModel selectModel;

        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }

        public SelectRenderer build() {
            return new SelectRenderer(this);
        }

        Builder getThis() {
            return this;
        }
    }
}
