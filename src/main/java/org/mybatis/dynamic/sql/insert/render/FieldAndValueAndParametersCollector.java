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
import java.util.stream.Collector;

public class FieldAndValueAndParametersCollector extends FieldAndValueCollector {
    private final Map<String, Object> parameters = new HashMap<>();

    public FieldAndValueAndParametersCollector() {
        super();
    }

    public void add(FieldAndValueAndParameters fieldAndValueAndParameters) {
        super.add(fieldAndValueAndParameters);
        parameters.putAll(fieldAndValueAndParameters.parameters());
    }

    public FieldAndValueAndParametersCollector merge(FieldAndValueAndParametersCollector other) {
        super.merge(other);
        parameters.putAll(other.parameters);
        return this;
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    public static Collector<
            FieldAndValueAndParameters,
            FieldAndValueAndParametersCollector,
            FieldAndValueAndParametersCollector> collectWithParameters() {
        return Collector.of(FieldAndValueAndParametersCollector::new,
                FieldAndValueAndParametersCollector::add,
                FieldAndValueAndParametersCollector::merge);
    }
}
