package examples.r2dbc.spring;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertSelectModel;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.r2dbc.core.binding.BindTarget;

public class OperationFactory {

    public static PreparedOperation<SelectStatementProvider> fromSelectModel(Buildable<SelectModel> selectModel) {
        SelectStatementProvider selectStatementProvider = selectModel.build().render(RenderingStrategies.R2DBC);

        return new PreparedOperation<SelectStatementProvider>() {
            @NotNull
            @Override
            public SelectStatementProvider getSource() {
                return selectStatementProvider;
            }

            @Override
            public void bindTo(@NotNull BindTarget target) {
                selectStatementProvider.getParameters().forEach(target::bind);
            }

            @NotNull
            @Override
            public String toQuery() {
                return selectStatementProvider.getSelectStatement();
            }
        };
    }

    public static PreparedOperation<DeleteStatementProvider> fromDeleteModel(Buildable<DeleteModel> deleteModel) {
        DeleteStatementProvider deleteStatementProvider = deleteModel.build().render(RenderingStrategies.R2DBC);

        return new PreparedOperation<DeleteStatementProvider>() {
            @NotNull
            @Override
            public DeleteStatementProvider getSource() {
                return deleteStatementProvider;
            }

            @Override
            public void bindTo(@NotNull BindTarget target) {
                deleteStatementProvider.getParameters().forEach(target::bind);
            }

            @NotNull
            @Override
            public String toQuery() {
                return deleteStatementProvider.getDeleteStatement();
            }
        };
    }

    public static PreparedOperation<UpdateStatementProvider> fromUpdateModel(Buildable<UpdateModel> updateModel) {
        UpdateStatementProvider updateStatementProvider = updateModel.build().render(RenderingStrategies.R2DBC);

        return new PreparedOperation<UpdateStatementProvider>() {
            @NotNull
            @Override
            public UpdateStatementProvider getSource() {
                return updateStatementProvider;
            }

            @Override
            public void bindTo(@NotNull BindTarget target) {
                updateStatementProvider.getParameters().forEach(target::bind);
            }

            @NotNull
            @Override
            public String toQuery() {
                return updateStatementProvider.getUpdateStatement();
            }
        };
    }

    public static PreparedOperation<GeneralInsertStatementProvider> fromGeneralInsertModel(
            Buildable<GeneralInsertModel> generalInsertModel) {
        GeneralInsertStatementProvider insertStatementProvider =
                generalInsertModel.build().render(RenderingStrategies.R2DBC);

        return new PreparedOperation<GeneralInsertStatementProvider>() {
            @NotNull
            @Override
            public GeneralInsertStatementProvider getSource() {
                return insertStatementProvider;
            }

            @Override
            public void bindTo(@NotNull BindTarget target) {
                insertStatementProvider.getParameters().forEach(target::bind);
            }

            @NotNull
            @Override
            public String toQuery() {
                return insertStatementProvider.getInsertStatement();
            }
        };
    }

    public static PreparedOperation<InsertSelectStatementProvider> fromInsertSelectModel(
            Buildable<InsertSelectModel> insertSelectModel) {
        InsertSelectStatementProvider insertSelectStatementProvider =
                insertSelectModel.build().render(RenderingStrategies.R2DBC);

        return new PreparedOperation<InsertSelectStatementProvider>() {
            @NotNull
            @Override
            public InsertSelectStatementProvider getSource() {
                return insertSelectStatementProvider;
            }

            @Override
            public void bindTo(@NotNull BindTarget target) {
                insertSelectStatementProvider.getParameters().forEach(target::bind);
            }

            @NotNull
            @Override
            public String toQuery() {
                return insertSelectStatementProvider.getInsertStatement();
            }
        };
    }
}
