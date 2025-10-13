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

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BindableColumn;

/**
 * Count functions are implemented differently than the other aggregates. This is primarily to preserve
 * backwards compatibility. Count functions are configured as BindableColumns of type Long
 * as it is assumed that the count functions always return a number.
 */
public abstract class AbstractCount<T extends AbstractCount<T>> implements BindableColumn<Long>, WindowDSL<T> {
    protected final @Nullable String alias;
    protected @Nullable WindowModel windowModel;

    protected AbstractCount(@Nullable String alias, @Nullable WindowModel windowModel) {
        this.alias = alias;
        this.windowModel = windowModel;
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public T over(WindowModel windowModel) {
        T copy = copy();
        copy.windowModel = windowModel;
        return copy;
    }

    protected abstract T copy();
}
