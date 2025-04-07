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

import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.AbstractTwoValueCondition;

public class IsNotBetweenWhenPresent<T> extends AbstractTwoValueCondition<T>
        implements AbstractTwoValueCondition.Filterable<T>, AbstractTwoValueCondition.Mappable<T> {
    private static final IsNotBetweenWhenPresent<?> EMPTY = new IsNotBetweenWhenPresent<Object>(-1, -1) {
        @Override
        public Object value1() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public Object value2() {
            throw new NoSuchElementException("No value present"); //$NON-NLS-1$
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    public static <T> IsNotBetweenWhenPresent<T> empty() {
        @SuppressWarnings("unchecked")
        IsNotBetweenWhenPresent<T> t = (IsNotBetweenWhenPresent<T>) EMPTY;
        return t;
    }

    protected IsNotBetweenWhenPresent(T value1, T value2) {
        super(value1, value2);
    }

    @Override
    public String operator1() {
        return "not between"; //$NON-NLS-1$
    }

    @Override
    public String operator2() {
        return "and"; //$NON-NLS-1$
    }

    @Override
    public IsNotBetweenWhenPresent<T> filter(BiPredicate<? super @NonNull T, ? super @NonNull T> predicate) {
        return filterSupport(predicate, IsNotBetweenWhenPresent::empty, this);
    }

    @Override
    public IsNotBetweenWhenPresent<T> filter(Predicate<? super @NonNull T> predicate) {
        return filterSupport(predicate, IsNotBetweenWhenPresent::empty, this);
    }

    @Override
    public <R> IsNotBetweenWhenPresent<R> map(Function<? super @NonNull T, ? extends @Nullable R> mapper1,
                                              Function<? super @NonNull T, ? extends @Nullable R> mapper2) {
        return mapSupport(mapper1, mapper2, IsNotBetweenWhenPresent::of, IsNotBetweenWhenPresent::empty);
    }

    @Override
    public <R> IsNotBetweenWhenPresent<R> map(Function<? super @NonNull T, ? extends @Nullable R> mapper) {
        return map(mapper, mapper);
    }

    public static <T> IsNotBetweenWhenPresent<T> of(@Nullable T value1, @Nullable T value2) {
        if (value1 == null || value2 == null) {
            return empty();
        } else {
            return new IsNotBetweenWhenPresent<>(value1, value2);
        }
    }

    public static <T> Builder<T> isNotBetweenWhenPresent(@Nullable T value1) {
        return new Builder<>(value1);
    }

    public static class Builder<T> extends AndWhenPresentGatherer<T, IsNotBetweenWhenPresent<T>> {

        private Builder(@Nullable T value1) {
            super(value1);
        }

        @Override
        protected IsNotBetweenWhenPresent<T> build(@Nullable T value2) {
            return IsNotBetweenWhenPresent.of(value1, value2);
        }
    }
}
