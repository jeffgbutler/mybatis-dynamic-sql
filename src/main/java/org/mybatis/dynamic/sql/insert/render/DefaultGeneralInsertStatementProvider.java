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
package org.mybatis.dynamic.sql.insert.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mybatis.dynamic.sql.render.ParameterBinding;
import org.mybatis.dynamic.sql.render.ParameterBindings;

public class DefaultGeneralInsertStatementProvider
        implements GeneralInsertStatementProvider, InsertSelectStatementProvider {
    private final String insertStatement;
    private final ParameterBindings parameters;

    private DefaultGeneralInsertStatementProvider(Builder builder) {
        insertStatement = Objects.requireNonNull(builder.insertStatement);
        parameters = new ParameterBindings(builder.parameterBindings);
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public List<ParameterBinding> getParameterBindings() {
        return parameters.getParameterBindings();
    }

    @Override
    public String getInsertStatement() {
        return insertStatement;
    }

    public static Builder withInsertStatement(String insertStatement) {
        return new Builder().withInsertStatement(insertStatement);
    }

    public static class Builder {
        private String insertStatement;
        private final List<ParameterBinding> parameterBindings = new ArrayList<>();

        public Builder withInsertStatement(String insertStatement) {
            this.insertStatement = insertStatement;
            return this;
        }

        public Builder withParameterBindings(List<ParameterBinding> parameterBindings) {
            this.parameterBindings.addAll(parameterBindings);
            return this;
        }

        public DefaultGeneralInsertStatementProvider build() {
            return new DefaultGeneralInsertStatementProvider(this);
        }
    }
}
