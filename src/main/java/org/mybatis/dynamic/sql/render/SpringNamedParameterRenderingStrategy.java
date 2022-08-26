/*
 *    Copyright 2016-2020 the original author or authors.
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

import org.mybatis.dynamic.sql.BindableColumn;

public class SpringNamedParameterRenderingStrategy extends RenderingStrategy {

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return getFormattedJdbcPlaceholder(prefix, parameterName);
    }

    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return ":" + parameterName; //$NON-NLS-1$
    }

    @Override
    public String getMultiRowFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return ":" + prefix + "." + parameterName; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
