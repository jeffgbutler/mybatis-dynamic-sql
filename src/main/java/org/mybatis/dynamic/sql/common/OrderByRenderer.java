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
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class OrderByRenderer {
    private final RenderingContext renderingContext;

    public OrderByRenderer(RenderingContext renderingContext) {
        this.renderingContext = Objects.requireNonNull(renderingContext);
    }

    public FragmentAndParameters render(OrderByModel orderByModel) {
        return orderByModel.columns().map(c -> c.renderForOrderBy(renderingContext))
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(
                        Collectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
