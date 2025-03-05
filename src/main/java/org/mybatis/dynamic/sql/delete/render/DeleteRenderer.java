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
package org.mybatis.dynamic.sql.delete.render;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.common.OrderByRenderer;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.EmbeddedWhereModel;

public class DeleteRenderer {
    private final DeleteModel deleteModel;
    private final RenderingContext renderingContext;
    private final DeleteRendererVisitor visitor;

    private DeleteRenderer(Builder builder) {
        deleteModel = Objects.requireNonNull(builder.deleteModel);
        TableAliasCalculator tableAliasCalculator = builder.deleteModel.tableAlias()
                .map(a -> ExplicitTableAliasCalculator.of(deleteModel.table(), a))
                .orElseGet(TableAliasCalculator::empty);
        renderingContext = RenderingContext
                .withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy))
                .withTableAliasCalculator(tableAliasCalculator)
                .withStatementConfiguration(deleteModel.statementConfiguration())
                .build();
        visitor = Objects.requireNonNull(builder.visitor);
        visitor.setRenderingContext(renderingContext);
    }

    public DeleteStatementProvider render() {
        FragmentCollector fragmentCollector = new FragmentCollector();

        fragmentCollector.add(calculateDeleteStatementStart());
        fragmentCollector.add(calculateTableName());
        calculateWhereClause().ifPresent(fragmentCollector::add);
        calculateOrderByClause().ifPresent(fragmentCollector::add);
        calculateLimitClause().ifPresent(fragmentCollector::add);
        visitor.visitStatementEnd().ifPresent(fragmentCollector::add);

        return toDeleteStatementProvider(fragmentCollector);
    }

    private DeleteStatementProvider toDeleteStatementProvider(FragmentCollector fragmentCollector) {
        return DefaultDeleteStatementProvider
                .withDeleteStatement(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private FragmentAndParameters calculateDeleteStatementStart() {
        return FragmentAndParameters.fromFragment("delete from") //$NON-NLS-1$
                .map(visitor::visitStatementStart);
    }

    private FragmentAndParameters calculateTableName() {
        String aliasedTableName = renderingContext.aliasedTableName(deleteModel.table());
        return FragmentAndParameters.fromFragment(aliasedTableName)
                .map(visitor::visitTable);
    }

    private Optional<FragmentAndParameters> calculateWhereClause() {
        return deleteModel.whereModel()
                .flatMap(this::renderWhereClause)
                .map(visitor::visitWhereClause);
    }

    private Optional<FragmentAndParameters> renderWhereClause(EmbeddedWhereModel whereModel) {
        return whereModel.render(renderingContext);
    }

    private Optional<FragmentAndParameters> calculateLimitClause() {
        return deleteModel.limit()
                .map(this::renderLimitClause)
                .map(visitor::visitLimitClause);
    }

    private FragmentAndParameters renderLimitClause(Long limit) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateLimitParameterInfo();

        return FragmentAndParameters.withFragment("limit " + parameterInfo.renderedPlaceHolder()) //$NON-NLS-1$
                .withParameter(parameterInfo.parameterMapKey(), limit)
                .build();
    }

    private Optional<FragmentAndParameters> calculateOrderByClause() {
        return deleteModel.orderByModel()
                .map(this::renderOrderByClause)
                .map(visitor::visitOrderByClause);
    }

    private FragmentAndParameters renderOrderByClause(OrderByModel orderByModel) {
        return new OrderByRenderer(renderingContext).render(orderByModel);
    }

    public static Builder withDeleteModel(DeleteModel deleteModel) {
        return new Builder().withDeleteModel(deleteModel);
    }

    public static class Builder {
        private @Nullable DeleteModel deleteModel;
        private @Nullable RenderingStrategy renderingStrategy;
        private @Nullable DeleteRendererVisitor visitor;

        public Builder withDeleteModel(DeleteModel deleteModel) {
            this.deleteModel = deleteModel;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withVisitor(DeleteRendererVisitor visitor) {
            this.visitor = visitor;
            return this;
        }

        public DeleteRenderer build() {
            return new DeleteRenderer(this);
        }
    }
}
