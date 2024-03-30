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
package org.mybatis.dynamic.sql.select.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mybatis.dynamic.sql.render.ParameterBinding;
import org.mybatis.dynamic.sql.render.ParameterBindings;

public class DefaultSelectStatementProvider implements SelectStatementProvider {
    private final String selectStatement;
    private final ParameterBindings parameterBindings;

    private DefaultSelectStatementProvider(Builder builder) {
        selectStatement = Objects.requireNonNull(builder.selectStatement);
        parameterBindings = new ParameterBindings(builder.parameterBindings);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameterBindings;
    }

    @Override
    public String getSelectStatement() {
        return selectStatement;
    }

    @Override
    public List<ParameterBinding> getParameterBindings() {
        return parameterBindings.getParameterBindings();
    }

    public static Builder withSelectStatement(String selectStatement) {
        return new Builder().withSelectStatement(selectStatement);
    }

    public static class Builder {
        private String selectStatement;
        private final List<ParameterBinding> parameterBindings = new ArrayList<>();

        public Builder withSelectStatement(String selectStatement) {
            this.selectStatement = selectStatement;
            return this;
        }

        public Builder withParameterBindings(List<ParameterBinding> parameterBindings) {
            this.parameterBindings.addAll(parameterBindings);
            return this;
        }

        public DefaultSelectStatementProvider build() {
            return new DefaultSelectStatementProvider(this);
        }
    }
}
