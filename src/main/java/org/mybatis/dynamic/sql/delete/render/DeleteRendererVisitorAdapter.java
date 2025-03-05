package org.mybatis.dynamic.sql.delete.render;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.render.RenderingContext;

public class DeleteRendererVisitorAdapter implements DeleteRendererVisitor {
    protected @Nullable RenderingContext renderingContext;

    @Override
    public void setRenderingContext(RenderingContext renderingContext) {
        this.renderingContext = renderingContext;
    }
}
