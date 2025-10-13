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
package org.mybatis.dynamic.sql.select.aggregate;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.RenderableCondition;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.Validator;
import org.mybatis.dynamic.sql.where.render.ColumnAndConditionRenderer;

public class Sum<T> extends AbstractAggregate<T, Sum<T>> {
    private final Function<RenderingContext, FragmentAndParameters> renderer;

    private Sum(BasicColumn column) {
        super(column, null, null);
        renderer = rc -> column.render(rc).mapFragment(this::applyAggregate);
    }

    private Sum(BindableColumn<T> column, RenderableCondition<T> condition) {
        super(column, null, null);
        renderer = rc -> {
            Validator.assertTrue(condition.shouldRender(rc), "ERROR.37", "sum"); //$NON-NLS-1$ //$NON-NLS-2$

            return new ColumnAndConditionRenderer.Builder<T>()
                    .withColumn(column)
                    .withCondition(condition)
                    .withRenderingContext(rc)
                    .build()
                    .render()
                    .mapFragment(this::applyAggregate);
        };
    }

    private Sum(BasicColumn column, Function<RenderingContext, FragmentAndParameters> renderer,
                @Nullable String alias, @Nullable WindowModel windowModel) {
        super(column, alias, windowModel);
        this.renderer = renderer;
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        FragmentCollector fragmentCollector = new FragmentCollector();
        fragmentCollector.add(renderer.apply(renderingContext));
        renderWindowModel(renderingContext).ifPresent(fragmentCollector::add);
        return fragmentCollector.toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    protected String applyAggregate(String s) {
        return "sum(" + s + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected Sum<T> copy() {
        return new Sum<>(column, renderer, alias, windowModel);
    }

    public static <T> Sum<T> of(BindableColumn<T> column) {
        return new Sum<>(column);
    }

    public static Sum<Object> of(BasicColumn column) {
        return new Sum<>(column);
    }

    public static <T> Sum<T> of(BindableColumn<T> column, RenderableCondition<T> condition) {
        return new Sum<>(column, condition);
    }
}
