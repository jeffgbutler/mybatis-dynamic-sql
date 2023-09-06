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
package org.mybatis.dynamic.sql.where.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.AbstractColumnComparisonCondition;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.AbstractNoValueCondition;
import org.mybatis.dynamic.sql.AbstractSingleValueCondition;
import org.mybatis.dynamic.sql.AbstractSubselectCondition;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.ConditionVisitor;
import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class DefaultConditionVisitor<T> implements ConditionVisitor<T, FragmentAndParameters> {

    private final BindableColumn<T> column;
    private final RenderingContext renderingContext;

    private DefaultConditionVisitor(Builder<T> builder) {
        column = Objects.requireNonNull(builder.column);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
    }

    @Override
    public FragmentAndParameters visit(AbstractListValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        FragmentCollector fc = condition.mapValues(this::toFragmentAndParameters).collect(FragmentCollector.collect());

        String joinedFragments =
                fc.collectFragments(Collectors.joining(",", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + spaceBefore(joinedFragments);

        return FragmentAndParameters
                .withFragment(finalFragment)
                .withParameters(fc.parameters())
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractNoValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator());
        return FragmentAndParameters.withFragment(finalFragment)
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSingleValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(column);
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + spaceBefore(parameterInfo.renderedPlaceHolder());

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameter(parameterInfo.parameterMapKey(), convertValue(condition.value()))
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractTwoValueCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        RenderedParameterInfo parameterInfo1 = renderingContext.calculateParameterInfo(column);
        RenderedParameterInfo parameterInfo2 = renderingContext.calculateParameterInfo(column);

        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator1())
                + spaceBefore(parameterInfo1.renderedPlaceHolder())
                + spaceBefore(condition.operator2())
                + spaceBefore(parameterInfo2.renderedPlaceHolder());

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameter(parameterInfo1.parameterMapKey(), convertValue(condition.value1()))
                .withParameter(parameterInfo2.parameterMapKey(), convertValue(condition.value2()))
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSubselectCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        SelectStatementProvider selectStatement = SelectRenderer.withSelectModel(condition.selectModel())
                .withRenderingContext(renderingContext)
                .build()
                .render();

        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + " (" //$NON-NLS-1$
                + selectStatement.getSelectStatement()
                + ")"; //$NON-NLS-1$

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameters(selectStatement.getParameters())
                .withParameters(renderedLeftColumn.parameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractColumnComparisonCondition<T> condition) {
        FragmentAndParameters renderedLeftColumn = column.render(renderingContext);
        FragmentAndParameters renderedRightColumn = condition.rightColumn().render(renderingContext);
        String finalFragment = condition.overrideRenderedLeftColumn(renderedLeftColumn.fragment())
                + spaceBefore(condition.operator())
                + spaceBefore(renderedRightColumn.fragment());
        return FragmentAndParameters.withFragment(finalFragment)
                .withParameters(renderedLeftColumn.parameters())
                .withParameters(renderedRightColumn.parameters())
                .build();
    }

    private Object convertValue(T value) {
        return column.convertParameterType(value);
    }

    private FragmentAndParameters toFragmentAndParameters(T value) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(column);
        return FragmentAndParameters.withFragment(parameterInfo.renderedPlaceHolder())
                .withParameter(parameterInfo.parameterMapKey(), convertValue(value))
                .build();
    }

    public static <T> Builder<T> withColumn(BindableColumn<T> column) {
        return new Builder<T>().withColumn(column);
    }

    public static class Builder<T> {
        private BindableColumn<T> column;
        private RenderingContext renderingContext;

        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }

        public Builder<T> withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public DefaultConditionVisitor<T> build() {
            return new DefaultConditionVisitor<>(this);
        }
    }
}
