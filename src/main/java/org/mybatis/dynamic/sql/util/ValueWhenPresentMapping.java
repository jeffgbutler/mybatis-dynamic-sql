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
package org.mybatis.dynamic.sql.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.SqlColumn;

public class ValueWhenPresentMapping<T> extends AbstractColumnMapping {

    private final Supplier<@Nullable T> valueSupplier;
    // keep a reference to the column so we don't lose the type
    private final SqlColumn<T> localColumn;

    private ValueWhenPresentMapping(SqlColumn<T> column, Supplier<@Nullable T> valueSupplier) {
        super(column);
        this.valueSupplier = Objects.requireNonNull(valueSupplier);
        localColumn = Objects.requireNonNull(column);
    }

    public Optional<Object> value() {
        return Optional.ofNullable(valueSupplier.get()).flatMap(this::convert);
    }

    private Optional<Object> convert(T value) {
        return Optional.ofNullable(localColumn.convertParameterType(value));
    }

    @Override
    public <R> R accept(ColumnMappingVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static <T> ValueWhenPresentMapping<T> of(SqlColumn<T> column, Supplier<@Nullable T> valueSupplier) {
        return new ValueWhenPresentMapping<>(column, valueSupplier);
    }
}
