/*
 *    Copyright 2016-2022 the original author or authors.
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
package org.mybatis.dynamic.sql.insert;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertRenderer;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.Messages;

public class MultiRowInsertModel<T> extends AbstractMultiRowInsertModel<T> {

    private MultiRowInsertModel(Builder<T> builder) {
        super(builder);
        if (records().isEmpty()) {
            throw new InvalidSqlException(Messages.getString("ERROR.20")); //$NON-NLS-1$
        }
        if (columnMappings.isEmpty()) {
            throw new InvalidSqlException(Messages.getString("ERROR.8")); //$NON-NLS-1$
        }
    }

    @NotNull
    public MultiRowInsertStatementProvider<T> render(RenderingStrategy renderingStrategy) {
        return MultiRowInsertRenderer.withMultiRowInsertModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static <T> Builder<T> withRecords(Collection<T> records) {
        return new Builder<T>().withRecords(records);
    }

    public static class Builder<T> extends AbstractBuilder<T, Builder<T>> {
        @Override
        protected Builder<T> getThis() {
            return this;
        }

        public MultiRowInsertModel<T> build() {
            return new MultiRowInsertModel<>(this);
        }
    }
}
