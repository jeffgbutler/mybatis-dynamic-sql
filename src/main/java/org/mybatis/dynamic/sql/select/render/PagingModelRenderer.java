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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class PagingModelRenderer {
    private final PagingModel pagingModel;
    private final RenderingContext renderingContext;

    private PagingModelRenderer(Builder builder) {
        renderingContext = Objects.requireNonNull(builder.renderingContext);
        pagingModel = Objects.requireNonNull(builder.pagingModel);
    }

    public FragmentAndParameters render() {
        return pagingModel.limit().map(this::limitAndOffsetRender)
                .orElseGet(this::fetchFirstRender);
    }

    private FragmentAndParameters limitAndOffsetRender(Long limit) {
        return new LimitAndOffsetPagingModelRenderer(renderingContext, limit, pagingModel).render();
    }

    private FragmentAndParameters fetchFirstRender() {
        return new FetchFirstPagingModelRenderer(renderingContext, pagingModel).render();
    }

    public static class Builder {
        private PagingModel pagingModel;
        private RenderingContext renderingContext;

        public Builder withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public Builder withPagingModel(PagingModel pagingModel) {
            this.pagingModel = pagingModel;
            return this;
        }

        public PagingModelRenderer build() {
            return new PagingModelRenderer(this);
        }
    }
}
