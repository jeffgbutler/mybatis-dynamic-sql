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

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.JDBCType;

import org.junit.jupiter.api.Test;

class ParameterBindingTest {

    @Test
    void testEquals1() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").build();

        assertThat(pb1.equals(null)).isFalse();
    }

    @Test
    void testEquals2() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").build();

        assertThat(pb1.equals(2)).isFalse();
    }

    @Test
    void testEquals3() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("1").build();

        assertThat(pb1.equals(pb2)).isTrue();
    }

    @Test
    void testEquals4() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("2").build();

        assertThat(pb1.equals(pb2)).isFalse();
    }

    @Test
    void testEquals5() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").withValue(1).build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("1").withValue(1).build();

        assertThat(pb1.equals(pb2)).isTrue();
    }

    @Test
    void testEquals6() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").withValue(1).build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("1").withValue(2).build();

        assertThat(pb1.equals(pb2)).isFalse();
    }

    @Test
    void testEquals7() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").withValue(1).withJdbcType(JDBCType.INTEGER).build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("1").withValue(1).withJdbcType(JDBCType.INTEGER).build();

        assertThat(pb1.equals(pb2)).isTrue();
    }

    @Test
    void testEquals8() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").withValue(1).withJdbcType(JDBCType.INTEGER).build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("1").withValue(1).withJdbcType(JDBCType.VARCHAR).build();

        assertThat(pb1.equals(pb2)).isFalse();
    }

    @Test
    void testHashCode1() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("1").build();

        assertThat(pb1).hasSameHashCodeAs(pb2);
    }

    @Test
    void testHashCode2() {
        ParameterBinding pb1 = ParameterBinding.withMapKey("1").withJdbcType(JDBCType.INTEGER).withValue(1).build();
        ParameterBinding pb2 = ParameterBinding.withMapKey("1").withJdbcType(JDBCType.INTEGER).withValue(1).build();

        assertThat(pb1).hasSameHashCodeAs(pb2);
    }
}
