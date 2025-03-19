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
package org.mybatis.dynamic.sql.insert.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

public class FieldAndValueAndParameters extends FieldAndValue {
    private final Map<String, Object> parameters;

    private FieldAndValueAndParameters(Builder builder) {
        super(builder.fieldName, builder.valuePhrase);
        parameters = builder.parameters;
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    public static Builder withFieldAndValue(String fieldName, String valuePhrase) {
        return new Builder(fieldName, valuePhrase);
    }

    public static class Builder {
        private final String fieldName;
        private final String valuePhrase;
        private final Map<String, Object> parameters = new HashMap<>();

        public Builder(String fieldName, String valuePhrase) {
            this.fieldName = fieldName;
            this.valuePhrase = valuePhrase;
        }
        public Builder withParameter(String key, @Nullable Object value) {
            // the value can be null because a parameter type converter may return null

            //noinspection DataFlowIssue
            parameters.put(key, value);
            return this;
        }

        public FieldAndValueAndParameters build() {
            return new FieldAndValueAndParameters(this);
        }

        public Optional<FieldAndValueAndParameters> buildOptional() {
            return Optional.of(build());
        }
    }
}
