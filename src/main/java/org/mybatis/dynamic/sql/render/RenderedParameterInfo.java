/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.render;

import java.sql.JDBCType;
import java.util.Objects;

public class RenderedParameterInfo {
    private final String parameterMapKey;
    private final String renderedPlaceHolder;

    public RenderedParameterInfo(String parameterMapKey, String renderedPlaceHolder) {
        this.parameterMapKey = Objects.requireNonNull(parameterMapKey);
        this.renderedPlaceHolder = Objects.requireNonNull(renderedPlaceHolder);
    }

    public String parameterMapKey() {
        return parameterMapKey;
    }

    public String renderedPlaceHolder() {
        return renderedPlaceHolder;
    }

    public ParameterBinding toParameterBinding(Object value) {
        return ParameterBinding.withMapKey(parameterMapKey())
                .withValue(value)
                .build();
    }

    public ParameterBinding toParameterBinding(Object value, JDBCType jdbcType) {
        return ParameterBinding.withMapKey(parameterMapKey())
                .withValue(value)
                .withJdbcType(jdbcType)
                .build();
    }
}
