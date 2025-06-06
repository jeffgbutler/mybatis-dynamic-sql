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

public abstract class UpdateMappingVisitor<R> implements ColumnMappingVisitor<R> {
    @Override
    public final R visit(PropertyMapping mapping) {
        throw new UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_10));
    }

    @Override
    public final R visit(PropertyWhenPresentMapping mapping) {
        throw new UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_11));
    }

    @Override
    public final R visit(RowMapping mapping) {
        throw new UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_15));
    }
}
