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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

public class FragmentAndParameters {

    private final String fragment;
    private final Map<String, Object> parameters;

    private FragmentAndParameters(Builder builder) {
        fragment = Objects.requireNonNull(builder.fragment);
        parameters = Collections.unmodifiableMap(builder.parameters);
    }

    public String fragment() {
        return fragment;
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    /**
     * Return a new instance with the same parameters and a transformed fragment.
     *
     * @param mapper a function that can change the value of the fragment
     * @return a new instance with the same parameters and a transformed fragment
     */
    public FragmentAndParameters mapFragment(UnaryOperator<String> mapper) {
        return withFragment(mapper.apply(fragment))
                .withParameters(parameters)
                .build();
    }

    public static Builder withFragment(String fragment) {
        return new Builder().withFragment(fragment);
    }

    public static FragmentAndParameters fromFragment(String fragment) {
        return new Builder().withFragment(fragment).build();
    }

    public static class Builder {
        private @Nullable String fragment;
        private final Map<String, Object> parameters = new HashMap<>();

        public Builder withFragment(String fragment) {
            this.fragment = fragment;
            return this;
        }

        public Builder withParameter(String key, @Nullable Object value) {
            // the value can be null because a parameter type converter may return null

            //noinspection DataFlowIssue
            parameters.put(key, value);
            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public FragmentAndParameters build() {
            return new FragmentAndParameters(this);
        }

        public Optional<FragmentAndParameters> buildOptional() {
            return Optional.of(build());
        }
    }
}
