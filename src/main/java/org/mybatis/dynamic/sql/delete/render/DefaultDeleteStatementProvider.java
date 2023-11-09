/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.delete.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mybatis.dynamic.sql.render.ParameterBinding;
import org.mybatis.dynamic.sql.render.ParameterBindings;

public class DefaultDeleteStatementProvider implements DeleteStatementProvider {
    private final String deleteStatement;
    private final ParameterBindings parameterBindings;

    private DefaultDeleteStatementProvider(Builder builder) {
        deleteStatement = Objects.requireNonNull(builder.deleteStatement);
        parameterBindings = new ParameterBindings(builder.parameterBindings);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameterBindings;
    }

    @Override
    public String getDeleteStatement() {
        return deleteStatement;
    }

    @Override
    public List<ParameterBinding> getParameterBindings() {
        return parameterBindings.getParameterBindings();
    }

    public static Builder withDeleteStatement(String deleteStatement) {
        return new Builder().withDeleteStatement(deleteStatement);
    }

    public static class Builder {
        private String deleteStatement;
        private final List<ParameterBinding> parameterBindings = new ArrayList<>();

        public Builder withDeleteStatement(String deleteStatement) {
            this.deleteStatement = deleteStatement;
            return this;
        }

        public Builder withParameterBindings(List<ParameterBinding> parameterBindings) {
            this.parameterBindings.addAll(parameterBindings);
            return this;
        }

        public DefaultDeleteStatementProvider build() {
            return new DefaultDeleteStatementProvider(this);
        }
    }
}
