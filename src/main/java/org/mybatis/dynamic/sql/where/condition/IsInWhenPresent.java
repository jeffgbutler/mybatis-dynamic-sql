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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractListValueCondition;

public class IsInWhenPresent<T> extends AbstractListValueCondition<T> {
    private static final IsInWhenPresent<?> EMPTY = new IsInWhenPresent<>(Collections.emptyList());

    public static <T> IsInWhenPresent<T> empty() {
        @SuppressWarnings("unchecked")
        IsInWhenPresent<T> t = (IsInWhenPresent<T>) EMPTY;
        return t;
    }

    protected IsInWhenPresent(Collection<T> values) {
        super(values.stream().filter(Objects::nonNull).toList());
    }

    @Override
    public String operator() {
        return "in"; //$NON-NLS-1$
    }

    @Override
    public IsInWhenPresent<T> filter(Predicate<? super T> predicate) {
        return filterSupport(predicate, IsInWhenPresent::new, this, IsInWhenPresent::empty);
    }

    /**
     * If not empty, apply the mapping to each value in the list return a new condition with the mapped values.
     *     Else return an empty condition (this).
     *
     * @param mapper a mapping function to apply to the values, if not empty
     * @param <R> type of the new condition
     * @return a new condition with mapped values if renderable, otherwise an empty condition
     */
    public <R> IsInWhenPresent<R> map(Function<? super T, ? extends R> mapper) {
        Function<Collection<R>, IsInWhenPresent<R>> constructor = IsInWhenPresent::new;
        return mapSupport(mapper, constructor, IsInWhenPresent::empty);
    }

    @SafeVarargs
    public static <T> IsInWhenPresent<T> of(@Nullable T... values) {
        return of(Arrays.asList(values));
    }

    public static <T> IsInWhenPresent<T> of(Collection<@Nullable T> values) {
        return new IsInWhenPresent<>(values);
    }
}
