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
package org.mybatis.dynamic.sql.insert;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class InsertStatementConfiguration {
    private @Nullable String afterKeywordFragment;
    private @Nullable String afterStatementFragment;
    private @Nullable String beforeStatementFragment;

    public Optional<String> afterKeywordFragment() {
        return Optional.ofNullable(afterKeywordFragment);
    }

    public Optional<String> afterStatementFragment() {
        return Optional.ofNullable(afterStatementFragment);
    }

    public Optional<String> beforeStatementFragment() {
        return Optional.ofNullable(beforeStatementFragment);
    }

    public InsertStatementConfiguration withSqlAfterKeyword(String sql) {
        this.afterKeywordFragment = sql;
        return this;
    }

    public InsertStatementConfiguration withSqlAfterStatement(String sql) {
        this.afterStatementFragment = sql;
        return this;
    }

    public InsertStatementConfiguration withSqlBeforeStatement(String sql) {
        this.beforeStatementFragment = sql;
        return this;
    }
}
