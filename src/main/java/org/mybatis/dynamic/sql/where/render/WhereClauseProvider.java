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
package org.mybatis.dynamic.sql.where.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mybatis.dynamic.sql.render.ParameterBinding;
import org.mybatis.dynamic.sql.render.ParameterBindings;

public class WhereClauseProvider {
    private final String whereClause;

    private final ParameterBindings parameterBindings;

    private WhereClauseProvider(Builder builder) {
        whereClause = Objects.requireNonNull(builder.whereClause);
        parameterBindings = new ParameterBindings(builder.parameterBindings);
    }

    public Map<String, Object> getParameters() {
        return parameterBindings;
    }

    public List<ParameterBinding> getParameterBindings() {
        return parameterBindings.getParameterBindings();
    }

    public String getWhereClause() {
        return whereClause;
    }

    public static Builder withWhereClause(String whereClause) {
        return new Builder().withWhereClause(whereClause);
    }

    public static class Builder {
        private String whereClause;
        private final List<ParameterBinding> parameterBindings = new ArrayList<>();

        public Builder withWhereClause(String whereClause) {
            this.whereClause = whereClause;
            return this;
        }

        public Builder withParameterBindings(List<ParameterBinding> parameterBindings) {
            this.parameterBindings.addAll(parameterBindings);
            return this;
        }

        public WhereClauseProvider build() {
            return new WhereClauseProvider(this);
        }
    }
}
