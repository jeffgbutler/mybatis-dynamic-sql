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
package org.mybatis.dynamic.sql.insert;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.dynamic.sql.insert.render.DefaultInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;

/**
 * This class contains the rendered fragments of an insert statement. The class allows a user to add fragments to
 * the generated insert statement or change the generated SQL fragments.
 *
 * <p>Unlike most other classes in this library, this class is mutable for ease of use. We caution users to be very
 * careful with modifications made through this class - with great power comes great responsibility!
 *
 * <p>In all cases, the composer will add spaces as necessary, so there is no need to append leading or trailing
 * spaces. It is also important to note that the library performs no validation of any changes made through this
 * class. The final statement can be visualized as follows:
 *
 * @since 1.5.1
 */
public class InsertStatementComposer<T> {
    private T row;
    private String initialFragment;
    private String startOfStatement;
    private String fragmentBeforeTable;
    private String tableFragment;
    private String fragmentAfterTable;
    private String columnsFragment;
    private String fragmentBeforeValues;
    private String valuesFragment;
    private String finalFragment;

    /**
     * Returns the row associated with this insert statement
     *
     * @return the row associated with this insert statement
     */
    public T getRow() {
        return row;
    }

    /**
     * Replace the row associated with this insert statement.
     *
     * @param row the replacement row
     */
    public void setRow(T row) {
        this.row = row;
    }

    public String getInitialFragment() {
        return initialFragment;
    }

    public void setInitialFragment(String initialFragment) {
        this.initialFragment = initialFragment;
    }

    public String getStartOfStatement() {
        return startOfStatement;
    }

    public void setStartOfStatement(String startOfStatement) {
        this.startOfStatement = startOfStatement;
    }

    public String getFragmentBeforeTable() {
        return fragmentBeforeTable;
    }

    public void setFragmentBeforeTable(String fragmentBeforeTable) {
        this.fragmentBeforeTable = fragmentBeforeTable;
    }

    public String getTableFragment() {
        return tableFragment;
    }

    public void setTableFragment(String tableFragment) {
        this.tableFragment = tableFragment;
    }

    public String getFragmentAfterTable() {
        return fragmentAfterTable;
    }

    public void setFragmentAfterTable(String fragmentAfterTable) {
        this.fragmentAfterTable = fragmentAfterTable;
    }

    public String getColumnsFragment() {
        return columnsFragment;
    }

    public void setColumnsFragment(String columnsFragment) {
        this.columnsFragment = columnsFragment;
    }

    public String getFragmentBeforeValues() {
        return fragmentBeforeValues;
    }

    public void setFragmentBeforeValues(String fragmentBeforeValues) {
        this.fragmentBeforeValues = fragmentBeforeValues;
    }

    public String getValuesFragment() {
        return valuesFragment;
    }

    public void setValuesFragment(String valuesFragment) {
        this.valuesFragment = valuesFragment;
    }

    public String getFinalFragment() {
        return finalFragment;
    }

    public void setFinalFragment(String finalFragment) {
        this.finalFragment = finalFragment;
    }

    public InsertStatementProvider<T> toStatementProvider() {
        return DefaultInsertStatementProvider.withRow(row)
                .withInsertStatement(calculateInsertStatement())
                .build();
    }

    private String calculateInsertStatement() {
        return Stream.of(initialFragment,
                        startOfStatement,
                        fragmentBeforeTable,
                        tableFragment,
                        fragmentAfterTable,
                        columnsFragment,
                        fragmentBeforeValues,
                        valuesFragment,
                        finalFragment
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" ")); //$NON-NLS-1$
    }

    /**
     * A Kotlin inspired utility method to make it easier to apply consumers
     *
     * @param consumer the consumer to apply to this composer
     * @return this composer
     */
    public InsertStatementComposer<T> apply(Consumer<InsertStatementComposer<T>> consumer) {
        consumer.accept(this);
        return this;
    }
}
