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
package org.mybatis.dynamic.sql.where.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

class RenderedCriterionTest {

    @Test
    void testSimpleCriteria() {
        RenderedCriterion rc = new RenderedCriterion.Builder()
                .withConnector("and")
                .withFragmentAndParameters(FragmentAndParameters.withFragment("col1 = :p1").build())
                .build();

        FragmentAndParameters fp = rc.fragmentAndParametersWithConnector();

        assertAll(
                () -> assertThat(fp.fragment()).isEqualTo("and col1 = :p1"),
                () -> assertThat(fp.parameters()).isEmpty()
        );
    }
}
