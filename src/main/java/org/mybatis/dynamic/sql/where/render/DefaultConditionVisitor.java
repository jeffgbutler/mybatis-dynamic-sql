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

import java.sql.JDBCType;
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
        FragmentCollector fc = condition.mapValues(this::toFragmentAndParameters).collect(FragmentCollector.collect());

        String joinedFragments =
                fc.collectFragments(Collectors.joining(",", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String finalFragment = condition.operator()
                + spaceBefore(joinedFragments);

        return FragmentAndParameters
                .withFragment(finalFragment)
                .withParameterBindings(fc.parameterBindings())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractNoValueCondition<T> condition) {
        return FragmentAndParameters.fromFragment(condition.operator());
    }

    @Override
    public FragmentAndParameters visit(AbstractSingleValueCondition<T> condition) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(column);
        String finalFragment = condition.operator()
                + spaceBefore(parameterInfo.renderedPlaceHolder());

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameterBinding(parameterInfo.toParameterBinding(convertValue(condition.value()),
                        calculateJdbcType()))
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractTwoValueCondition<T> condition) {
        RenderedParameterInfo parameterInfo1 = renderingContext.calculateParameterInfo(column);
        RenderedParameterInfo parameterInfo2 = renderingContext.calculateParameterInfo(column);

        String finalFragment = condition.operator1()
                + spaceBefore(parameterInfo1.renderedPlaceHolder())
                + spaceBefore(condition.operator2())
                + spaceBefore(parameterInfo2.renderedPlaceHolder());
        return FragmentAndParameters.withFragment(finalFragment)
                .withParameterBinding(parameterInfo1.toParameterBinding(convertValue(condition.value1()),
                        calculateJdbcType()))
                .withParameterBinding(parameterInfo2.toParameterBinding(convertValue(condition.value2()),
                        calculateJdbcType()))
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractSubselectCondition<T> condition) {
        SelectStatementProvider selectStatement = condition.selectModel().render(renderingContext);

        String finalFragment = condition.operator()
                + " (" //$NON-NLS-1$
                + selectStatement.getSelectStatement()
                + ")"; //$NON-NLS-1$

        return FragmentAndParameters.withFragment(finalFragment)
                .withParameterBindings(selectStatement.getParameterBindings())
                .build();
    }

    @Override
    public FragmentAndParameters visit(AbstractColumnComparisonCondition<T> condition) {
        FragmentAndParameters renderedRightColumn = condition.rightColumn().render(renderingContext);
        String finalFragment = condition.operator()
                + spaceBefore(renderedRightColumn.fragment());
        return FragmentAndParameters.withFragment(finalFragment)
                .withParameterBindings(renderedRightColumn.parameterBindings())
                .build();
    }

    private Object convertValue(T value) {
        return column.convertParameterType(value);
    }

    private FragmentAndParameters toFragmentAndParameters(T value) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(column);
        return FragmentAndParameters.withFragment(parameterInfo.renderedPlaceHolder())
                .withParameterBinding(parameterInfo.toParameterBinding(convertValue(value), calculateJdbcType()))
                .build();
    }

    private JDBCType calculateJdbcType() {
        return column.jdbcType().orElse(null);
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
