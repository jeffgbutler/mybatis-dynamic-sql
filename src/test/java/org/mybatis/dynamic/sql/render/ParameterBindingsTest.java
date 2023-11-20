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
import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ParameterBindingsTest {

    @Test
    void testContainsValue() {
        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        pb.put("1", 1);

        assertThat(pb)
                .containsValue(1)
                .doesNotContainValue(2);
    }

    @Test
    void testPutDuplicate() {
        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        pb.put("1", 1);
        assertThat(pb.put("1", 2)).isEqualTo(1);
    }

    @Test
    void testPutAll() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("1", 1);
        inputMap.put("2", 2);

        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        pb.putAll(inputMap);
        assertThat(pb).hasSize(2);
    }

    @Test
    void testRemove() {
        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        pb.put("1", 1);
        pb.put("2", 2);
        Object prev = pb.remove("2");

        assertThat(prev).isEqualTo(2);
        assertThat(pb).hasSize(1);
        assertThat(pb).containsOnly(entry("1", 1));
    }

    @Test
    void testRemoveOnEmpty() {
        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        Object prev = pb.remove("1");

        assertThat(prev).isNull();
        assertThat(pb).isEmpty();
    }

    @Test
    void testClear() {
        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        pb.put("1", 1);
        assertThat(pb).hasSize(1);

        pb.clear();
        assertThat(pb).isEmpty();
    }

    @Test
    void testValues() {
        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        pb.put("1", 1);
        pb.put("2", 2);

        Collection<Object> values = pb.values();
        assertThat(values)
                .hasSize(2)
                .containsOnly(1, 2);
    }

    @Test
    void testUpdateEntry() {
        ParameterBindings pb = new ParameterBindings(new ArrayList<>());

        pb.put("1", 1);

        assertThat(pb).contains(entry("1", 1));

        Set<Map.Entry<String, Object>> entrySet = pb.entrySet();

        Optional<Map.Entry<String, Object>> optionalEntry = entrySet.stream().findFirst();
        assertThat(optionalEntry).isPresent();
        Map.Entry<String, Object> entry = optionalEntry.get();
        assertThat(entry.setValue(3)).isEqualTo(1);

        assertThat(pb).contains(entry("1", 3));
    }
}
