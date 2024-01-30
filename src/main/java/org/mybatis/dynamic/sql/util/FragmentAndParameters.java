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
package org.mybatis.dynamic.sql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.mybatis.dynamic.sql.render.ParameterBinding;

public class FragmentAndParameters {

    private final String fragment;
    private final List<ParameterBinding> parameterBindings;

    private FragmentAndParameters(Builder builder) {
        fragment = Objects.requireNonNull(builder.fragment);
        parameterBindings = builder.parameterBindings;
    }

    public String fragment() {
        return fragment;
    }

    public List<ParameterBinding> parameterBindings() {
        return parameterBindings;
    }

    /**
     * Return a new instance with the same parameters and a transformed fragment.
     *
     * @param mapper a function that can change the value of the fragment
     * @return a new instance with the same parameters and a transformed fragment
     */
    public FragmentAndParameters mapFragment(UnaryOperator<String> mapper) {
        return FragmentAndParameters.withFragment(mapper.apply(fragment))
                .withParameterBindings(parameterBindings)
                .build();
    }

    public static Builder withFragment(String fragment) {
        return new Builder().withFragment(fragment);
    }

    public static FragmentAndParameters fromFragment(String fragment) {
        return new Builder().withFragment(fragment).build();
    }

    public static class Builder {
        private String fragment;
        private final List<ParameterBinding> parameterBindings = new ArrayList<>();

        public Builder withFragment(String fragment) {
            this.fragment = fragment;
            return this;
        }

        public Builder withParameterBinding(ParameterBinding parameterBinding) {
            parameterBindings.add(parameterBinding);
            return this;
        }

        public Builder withParameterBindings(List<ParameterBinding> parameterBindings) {
            this.parameterBindings.addAll(parameterBindings);
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
