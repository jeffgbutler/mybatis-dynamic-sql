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
package org.mybatis.dynamic.sql.select.aggregate;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;

public class CountDistinct extends AbstractAggregate<Long, CountDistinct> {

    private CountDistinct(BasicColumn column, @Nullable String alias, @Nullable WindowModel windowModel) {
        super(column, alias, windowModel);
    }

    @Override
    protected CountDistinct copy() {
        return new CountDistinct(column, alias, windowModel);
    }

    @Override
    protected String applyAggregate(String columnName) {
        return "count(distinct " + columnName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static CountDistinct of(BasicColumn column) {
        return new CountDistinct(column, null, null);
    }
}
