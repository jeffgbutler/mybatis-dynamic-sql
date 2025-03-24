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
package org.mybatis.dynamic.sql.configuration;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.Renderable;
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

/**
 * This class can be used to change some behaviors of the framework. Every configurable statement
 * contains a unique instance of this class, so changes here will only impact a single statement.
 * If you intend to change the behavior for all statements, use the {@link GlobalConfiguration}.
 * Initial values for this class in each statement are set from the {@link GlobalConfiguration}.
 * Configurable behaviors are detailed below:
 *
 * <dl>
 *     <dt>nonRenderingWhereClauseAllowed</dt>
 *     <dd>If false (default), the framework will throw a {@link NonRenderingWhereClauseException}
 *         if a where clause is specified in the statement, but it fails to render because all
 *         optional conditions do not render. For example, if an "in" condition specifies an
 *         empty list of values. If no criteria are specified in a where clause, the framework
 *         assumes that no where clause was intended and will not throw an exception.
 *     </dd>
 * </dl>
 *
 * @see GlobalConfiguration
 *
 * @since 1.4.1
 *
 * @author Jeff Butler
 */
public class StatementConfiguration {
    private boolean isNonRenderingWhereClauseAllowed =
            GlobalContext.getConfiguration().isIsNonRenderingWhereClauseAllowed();
    private @Nullable Renderable afterKeywordFragment;
    private @Nullable Renderable afterStatementFragment;
    private @Nullable Renderable beforeStatementFragment;

    public boolean isNonRenderingWhereClauseAllowed() {
        return isNonRenderingWhereClauseAllowed;
    }

    public Optional<Renderable> afterKeywordFragment() {
        return Optional.ofNullable(afterKeywordFragment);
    }

    public Optional<Renderable> afterStatementFragment() {
        return Optional.ofNullable(afterStatementFragment);
    }

    public Optional<Renderable> beforeStatementFragment() {
        return Optional.ofNullable(beforeStatementFragment);
    }

    public StatementConfiguration setNonRenderingWhereClauseAllowed(boolean nonRenderingWhereClauseAllowed) {
        isNonRenderingWhereClauseAllowed = nonRenderingWhereClauseAllowed;
        return this;
    }

    public StatementConfiguration withSqlAfterKeyword(String sql) {
        return withSqlAfterKeyword(rc -> FragmentAndParameters.fromFragment(sql));
    }

    public StatementConfiguration withSqlAfterKeyword(Renderable renderable) {
        afterKeywordFragment = renderable;
        return this;
    }


    public StatementConfiguration withSqlAfterStatement(String sql) {
        return withSqlAfterStatement(rc -> FragmentAndParameters.fromFragment(sql));
    }

    public StatementConfiguration withSqlAfterStatement(Renderable renderable) {
        afterStatementFragment = renderable;
        return this;
    }

    public StatementConfiguration withSqlBeforeStatement(String sql) {
        return withSqlBeforeStatement(rc -> FragmentAndParameters.fromFragment(sql));
    }

    public StatementConfiguration withSqlBeforeStatement(Renderable renderable) {
        beforeStatementFragment = renderable;
        return this;
    }
}
