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
package org.mybatis.dynamic.sql.insert.render;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.SqlKeywords;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.Validator;

public class GeneralInsertRenderer {

    private final GeneralInsertModel model;
    private final GeneralInsertValuePhraseVisitor visitor;
    private final RenderingContext renderingContext;

    private GeneralInsertRenderer(Builder builder) {
        model = Objects.requireNonNull(builder.model);
        renderingContext = RenderingContext
                .withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy))
                .withStatementConfiguration(model.statementConfiguration())
                .build();
        visitor = new GeneralInsertValuePhraseVisitor(renderingContext);
    }

    public GeneralInsertStatementProvider render() {
        Optional<FragmentAndParameters> beforeStatementFragment = calculateBeforeStatementFragment();
        Optional<FragmentAndParameters> afterKeywordFragment = calculateAfterKeywordFragment();

        FieldAndValueAndParametersCollector collector = model.columnMappings()
                .map(m -> m.accept(visitor))
                .flatMap(Optional::stream)
                .collect(FieldAndValueAndParametersCollector.collectWithParameters());

        Validator.assertFalse(collector.isEmpty(), "ERROR.9"); //$NON-NLS-1$

        Optional<FragmentAndParameters> afterStatementFragment = calculateAfterStatementFragment();

        DefaultGeneralInsertStatementProvider.Builder builder = new DefaultGeneralInsertStatementProvider.Builder();

        StringJoiner joiner = new StringJoiner(" "); //$NON-NLS-1$

        beforeStatementFragment.ifPresent(fp -> {
            joiner.add(fp.fragment());
            builder.withParameters(fp.parameters());
        });
        joiner.add(SqlKeywords.INSERT);
        afterKeywordFragment.ifPresent(fp -> {
            joiner.add(fp.fragment());
            builder.withParameters(fp.parameters());
        });
        joiner.add(SqlKeywords.INTO);
        joiner.add(model.table().tableName());
        joiner.add(collector.columnsPhrase());
        joiner.add(collector.valuesPhrase());
        afterStatementFragment.ifPresent(fp -> {
            joiner.add(fp.fragment());
            builder.withParameters(fp.parameters());
        });

        return builder.withInsertStatement(joiner.toString())
                .withParameters(collector.parameters())
                .build();
    }

    // TODO: Duplicate
    private Optional<FragmentAndParameters> calculateBeforeStatementFragment() {
        return model.statementConfiguration().beforeStatementFragment().map(f -> f.render(renderingContext));
    }

    private Optional<FragmentAndParameters> calculateAfterKeywordFragment() {
        return model.statementConfiguration().afterKeywordFragment().map(f -> f.render(renderingContext));
    }

    private Optional<FragmentAndParameters> calculateAfterStatementFragment() {
        return model.statementConfiguration().afterStatementFragment().map(f -> f.render(renderingContext));
    }

    public static Builder withInsertModel(GeneralInsertModel model) {
        return new Builder().withInsertModel(model);
    }

    public static class Builder {
        private @Nullable GeneralInsertModel model;
        private @Nullable RenderingStrategy renderingStrategy;

        public Builder withInsertModel(GeneralInsertModel model) {
            this.model = model;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public GeneralInsertRenderer build() {
            return new GeneralInsertRenderer(this);
        }
    }
}
