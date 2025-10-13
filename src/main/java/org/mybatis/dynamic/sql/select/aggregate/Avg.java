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

import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class Avg<T> extends AbstractAggregate<T, Avg<T>> {

    private Avg(BasicColumn column, @Nullable String alias, @Nullable WindowModel windowModel) {
        super(column, alias, windowModel);
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        FragmentCollector fragmentCollector = new FragmentCollector();
        fragmentCollector.add(column.render(renderingContext)
                .mapFragment(s -> "avg(" + s + ")")); //$NON-NLS-1$ //$NON-NLS-2$
        renderWindowModel(renderingContext).ifPresent(fragmentCollector::add);
        return fragmentCollector.toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    @Override
    protected Avg<T> copy() {
        return new Avg<>(column, alias, windowModel);
    }

    public static <T> Avg<T> of(BindableColumn<T> column) {
        return new Avg<>(column, null, null);
    }
}
