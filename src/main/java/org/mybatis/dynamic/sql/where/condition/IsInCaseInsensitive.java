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
package org.mybatis.dynamic.sql.where.condition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.mybatis.dynamic.sql.AbstractListValueCondition;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.StringUtilities;
import org.mybatis.dynamic.sql.util.Validator;

public class IsInCaseInsensitive<T> extends AbstractListValueCondition<T>
        implements CaseInsensitiveRenderableCondition<T>, AbstractListValueCondition.Filterable<T>,
        AbstractListValueCondition.Mappable<T> {
    private static final IsInCaseInsensitive<?> EMPTY = new IsInCaseInsensitive<>(Collections.emptyList());

    public static <T> IsInCaseInsensitive<T> empty() {
        @SuppressWarnings("unchecked")
        IsInCaseInsensitive<T> t = (IsInCaseInsensitive<T>) EMPTY;
        return t;
    }

    protected IsInCaseInsensitive(Collection<T> values) {
        super(values.stream().map(StringUtilities::upperCaseIfPossible).toList());
    }

    @Override
    public boolean shouldRender(RenderingContext renderingContext) {
        Validator.assertNotEmpty(values, "ERROR.44", "IsInCaseInsensitive"); //$NON-NLS-1$ //$NON-NLS-2$
        return true;
    }

    @Override
    public String operator() {
        return "in"; //$NON-NLS-1$
    }

    @Override
    public IsInCaseInsensitive<T> filter(Predicate<? super @NonNull T> predicate) {
        return filterSupport(predicate, IsInCaseInsensitive::new, this, IsInCaseInsensitive::empty);
    }

    @Override
    public <R> IsInCaseInsensitive<R> map(Function<? super @NonNull T, ? extends @NonNull R> mapper) {
        return mapSupport(mapper, IsInCaseInsensitive::new, IsInCaseInsensitive::empty);
    }

    @SafeVarargs
    public static <T> IsInCaseInsensitive<T> of(T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsInCaseInsensitive<T> of(Collection<T> values) {
        return new IsInCaseInsensitive<>(values);
    }
}
