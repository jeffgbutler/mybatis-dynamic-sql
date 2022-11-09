/*
 *    Copyright 2016-2022 the original author or authors.
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

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.insert.InsertColumnListModel;
import org.mybatis.dynamic.sql.insert.InsertSelectModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class InsertSelectRenderer {

    private final InsertSelectModel model;
    private final RenderingStrategy renderingStrategy;

    private InsertSelectRenderer(Builder builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }

    public InsertSelectStatementProvider render() {
        SelectStatementProvider selectStatement = model.selectModel().render(renderingStrategy);

        String statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table());
        Optional<String> columnsPhrase = calculateColumnsPhrase();
        String renderedSelectStatement = selectStatement.getSelectStatement();

        String insertStatement = statementStart
                + columnsPhrase.map(StringUtilities::spaceBefore).orElse("") //$NON-NLS-1$
                + spaceBefore(renderedSelectStatement);

        return DefaultGeneralInsertStatementProvider.withInsertStatement(insertStatement)
                .withParameters(selectStatement.getParameters())
                .build();
    }

    private Optional<String> calculateColumnsPhrase() {
        return model.columnList().map(this::calculateColumnsPhrase);
    }

    private String calculateColumnsPhrase(InsertColumnListModel columnList) {
        return columnList.mapColumns(SqlColumn::name)
                .collect(Collectors.joining(", ", "(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static Builder withInsertSelectModel(InsertSelectModel model) {
        return new Builder().withInsertSelectModel(model);
    }

    public static class Builder {
        private InsertSelectModel model;
        private RenderingStrategy renderingStrategy;

        public Builder withInsertSelectModel(InsertSelectModel model) {
            this.model = model;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public InsertSelectRenderer build() {
            return new InsertSelectRenderer(this);
        }
    }
}
