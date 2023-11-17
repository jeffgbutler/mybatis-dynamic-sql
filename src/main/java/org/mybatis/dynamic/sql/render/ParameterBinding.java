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

import java.sql.JDBCType;
import java.util.Objects;
import java.util.Optional;

public class ParameterBinding {
    private final String mapKey;
    private Object value;
    private final JDBCType jdbcType;

    private ParameterBinding(Builder builder) {
        mapKey = Objects.requireNonNull(builder.mapKey);
        value = builder.value;
        jdbcType = builder.jdbcType;
    }

    public String getMapKey() {
        return mapKey;
    }

    public Object getValue() {
        return value;
    }

    public Object replaceValue(Object value) {
        Object existingValue = this.value;
        this.value = value;
        return existingValue;
    }

    public Optional<JDBCType> getJdbcType() {
        return Optional.ofNullable(jdbcType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParameterBinding that = (ParameterBinding) o;

        if (!mapKey.equals(that.mapKey)) {
            return false;
        }

        if (!Objects.equals(value, that.value)) {
            return false;
        }

        return jdbcType == that.jdbcType;
    }

    @Override
    public int hashCode() {
        int result = mapKey.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (jdbcType != null ? jdbcType.hashCode() : 0);
        return result;
    }

    public static Builder withMapKey(String mapKey) {
        return new Builder().withMapKey(mapKey);
    }

    public static class Builder {
        private String mapKey;
        private Object value;
        private JDBCType jdbcType;

        public Builder withMapKey(String mapKey) {
            this.mapKey = mapKey;
            return this;
        }

        public Builder withValue(Object value) {
            this.value = value;
            return this;
        }

        public Builder withJdbcType(JDBCType jdbcType) {
            this.jdbcType = jdbcType;
            return this;
        }

        public ParameterBinding build() {
            return new ParameterBinding(this);
        }
    }
}
