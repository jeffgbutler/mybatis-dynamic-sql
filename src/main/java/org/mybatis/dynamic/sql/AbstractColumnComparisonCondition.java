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
package org.mybatis.dynamic.sql;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public abstract class AbstractColumnComparisonCondition<T> implements RenderableCondition<T> {

    protected final BasicColumn rightColumn;

    protected AbstractColumnComparisonCondition(BasicColumn rightColumn) {
        this.rightColumn = rightColumn;
    }

    public abstract String operator();

    @Override
    public FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        return rightColumn.render(renderingContext).mapFragment(f -> operator() + spaceBefore(f));
    }
}
