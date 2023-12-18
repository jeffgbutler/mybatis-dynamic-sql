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
package org.mybatis.dynamic.sql.delete;

import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.AbstractStatementComposer;
import org.mybatis.dynamic.sql.delete.render.DefaultDeleteStatementProvider;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

/**
 * This class contains the rendered fragments of a delete statement. The class allows a user to add fragments to
 * the generated delete statement or change the generated SQL fragments.
 *
 * <p>Unlike most other classes in this library, this class is mutable for ease of use. We caution users to be very
 * careful with modifications made through this class - with great power comes great responsibility!
 *
 * In all cases, the composer will add spaces as necessary, so there is no need to append leading or trailing
 * spaces. It is also important to note that the library performs no validation of any changes made through this
 * class. The final statement can be visualized as follows:
 *
 * @since 1.5.1
 */
public class DeleteStatementComposer extends AbstractStatementComposer<DeleteStatementComposer> {

    private FragmentAndParameters whereClause;
    private FragmentAndParameters orderByClause;
    private FragmentAndParameters limitClause;

    public FragmentAndParameters getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(FragmentAndParameters whereClause) {
        this.whereClause = whereClause;
    }

    public FragmentAndParameters getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(FragmentAndParameters orderByClause) {
        this.orderByClause = orderByClause;
    }

    public FragmentAndParameters getLimitClause() {
        return limitClause;
    }

    public void setLimitClause(FragmentAndParameters limitClause) {
        this.limitClause = limitClause;
    }

    /**
     * Composes a statement and parameters that can be passed to a runtime for execution.
     *
     * @return a statement and parameters
     */
    public DeleteStatementProvider toStatementProvider() {
        FragmentCollector fragmentCollector = new FragmentCollector();

        fragmentCollector.addIfNonNull(initialFragment);
        fragmentCollector.addIfNonNull(startOfStatement);
        fragmentCollector.addIfNonNull(fragmentBeforeTable);
        fragmentCollector.addIfNonNull(tableFragment);
        fragmentCollector.addIfNonNull(fragmentAfterTable);
        fragmentCollector.addIfNonNull(whereClause);
        fragmentCollector.addIfNonNull(orderByClause);
        fragmentCollector.addIfNonNull(limitClause);
        fragmentCollector.addIfNonNull(finalFragment);

        return toDeleteStatementProvider(fragmentCollector);
    }

    private DeleteStatementProvider toDeleteStatementProvider(FragmentCollector fragmentCollector) {
        return DefaultDeleteStatementProvider
                .withDeleteStatement(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    @Override
    protected DeleteStatementComposer getThis() {
        return this;
    }
}
