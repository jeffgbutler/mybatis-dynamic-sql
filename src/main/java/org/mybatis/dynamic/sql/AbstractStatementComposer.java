/*
 *    Copyright 2016-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql;

import java.util.function.Consumer;

import org.mybatis.dynamic.sql.util.FragmentAndParameters;

/**
 * Base class for all statement composers.
 *
 * @param <T> subclass of this composer
 */
public abstract class AbstractStatementComposer<T extends AbstractStatementComposer<T>> {
    protected FragmentAndParameters initialFragment;
    protected FragmentAndParameters startOfStatement;
    protected FragmentAndParameters fragmentBeforeTable;
    protected FragmentAndParameters tableFragment;
    protected FragmentAndParameters fragmentAfterTable;
    protected FragmentAndParameters finalFragment;

    public FragmentAndParameters getInitialFragment() {
        return initialFragment;
    }

    public void setInitialFragment(FragmentAndParameters initialFragment) {
        this.initialFragment = initialFragment;
    }

    public FragmentAndParameters getStartOfStatement() {
        return startOfStatement;
    }

    public void setStartOfStatement(FragmentAndParameters startOfStatement) {
        this.startOfStatement = startOfStatement;
    }

    public FragmentAndParameters getFragmentBeforeTable() {
        return fragmentBeforeTable;
    }

    public void setFragmentBeforeTable(FragmentAndParameters fragmentBeforeTable) {
        this.fragmentBeforeTable = fragmentBeforeTable;
    }

    public FragmentAndParameters getTableFragment() {
        return tableFragment;
    }

    public void setTableFragment(FragmentAndParameters tableFragment) {
        this.tableFragment = tableFragment;
    }

    public FragmentAndParameters getFragmentAfterTable() {
        return fragmentAfterTable;
    }

    public void setFragmentAfterTable(FragmentAndParameters fragmentAfterTable) {
        this.fragmentAfterTable = fragmentAfterTable;
    }

    public FragmentAndParameters getFinalFragment() {
        return finalFragment;
    }

    public void setFinalFragment(FragmentAndParameters finalFragment) {
        this.finalFragment = finalFragment;
    }

    /**
     * A Kotlin inspired utility method to make it easier to apply consumers
     *
     * @param consumer the consumer to apply to this composer
     * @return this composer
     */
    public T apply(Consumer<T> consumer) {
        T self = getThis();
        consumer.accept(self);
        return self;
    }

    protected abstract T getThis();
}
