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
package org.mybatis.dynamic.sql.where;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.Buildable;

public class WhereDSL extends AbstractWhereStarter<WhereDSL.WhereFinisher, WhereDSL> implements Buildable<WhereModel> {
    private WhereFinisher whereBuilder;
    private final StatementConfiguration statementConfiguration = new StatementConfiguration();

    public WhereDSL() { }

    @Override
    public WhereFinisher where() {
        if (whereBuilder == null) {
            whereBuilder = new WhereFinisher();
        }
        return whereBuilder;
    }

    @NotNull
    @Override
    public WhereModel build() {
        return whereBuilder.buildModel();
    }

    @Override
    public WhereDSL configureStatement(Consumer<StatementConfiguration> consumer) {
        consumer.accept(statementConfiguration);
        return this;
    }

    public class WhereFinisher extends AbstractWhereFinisher<WhereFinisher> implements Buildable<WhereModel> {
        private WhereFinisher() {
            super(statementConfiguration);
        }

        @Override
        protected WhereFinisher getThis() {
            return this;
        }

        @NotNull
        @Override
        public WhereModel build() {
            return WhereDSL.this.build();
        }
    }
}
