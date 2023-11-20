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
package org.mybatis.dynamic.sql.render;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParameterBindings extends AbstractMap<String, Object> {

    private final List<ParameterBinding> parameterBindingList;

    public ParameterBindings(List<ParameterBinding> parameterBindingList) {
        this.parameterBindingList = Objects.requireNonNull(parameterBindingList);
    }

    public List<ParameterBinding> getParameterBindings() {
        return parameterBindingList;
    }

    @Nullable
    @Override
    public Object put(String key, Object value) {
        return findEntry(key)
                .map(pb -> pb.replaceValue(value))
                .orElseGet(() -> {
                    parameterBindingList.add(toBinding(key, value));
                    return null;
                });
    }

    private Optional<ParameterBinding> findEntry(Object key) {
        return parameterBindingList.stream().filter(pb -> Objects.equals(pb.getMapKey(), key)).findFirst();
    }

    @Nullable
    @Override
    public Object remove(Object key) {
        Object value = null;
        Iterator<ParameterBinding> iter = parameterBindingList.iterator();
        while (iter.hasNext()) {
            ParameterBinding parameterBinding = iter.next();
            if (Objects.equals(parameterBinding.getMapKey(), key)) {
                value = parameterBinding.getValue();
                iter.remove();
                break;
            }
        }

        return value;
    }

    @Override
    public void clear() {
        parameterBindingList.clear();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return parameterBindingList.stream()
                .map(this::toEntry)
                .collect(Collectors.toSet());
    }

    private Entry<String, Object> toEntry(ParameterBinding parameterBinding) {
        return new Entry<String, Object>() {
            @Override
            public String getKey() {
                return parameterBinding.getMapKey();
            }

            @Override
            public Object getValue() {
                return parameterBinding.getValue();
            }

            @Override
            public Object setValue(Object value) {
                return parameterBinding.replaceValue(value);
            }
        };
    }

    private ParameterBinding toBinding(String key, Object value) {
        return ParameterBinding.withMapKey(key)
                .withValue(value)
                .build();
    }
}
