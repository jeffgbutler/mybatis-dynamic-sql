package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.function.Consumer;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteRendererCustomizer;
import org.mybatis.dynamic.sql.delete.render.DeleteRendererVisitorAdapter;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

@NullMarked
class RenderVisitorTest {
    @Test
    void testIt() {
        class MyVisitor extends DeleteRendererVisitorAdapter {
            @Override
            public FragmentAndParameters visitStatementStart(FragmentAndParameters fragmentAndParameters) {
                return fragmentAndParameters.mapFragment(f -> "/* this is a comment */ " + f);
            }
        }

        DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                .where(id, isLessThan(22))
                .build()
                .render(RenderingStrategies.MYBATIS3, new MyVisitor());

        assertThat(deleteStatement.getDeleteStatement())
                .isEqualTo("/* this is a comment */ delete from AnimalData where id < #{parameters.p1,jdbcType=INTEGER}");
    }

    @Test
    void testIt2() {
        DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                .where(id, isLessThan(22))
                .build()
                .render(RenderingStrategies.MYBATIS3, (Consumer<DeleteRendererCustomizer>) c -> {
                    c.customizeStartOfStatement(fp -> fp.mapFragment("/* this is a comment */ {0}"));
                });

        assertThat(deleteStatement.getDeleteStatement())
                .isEqualTo("/* this is a comment */ delete from AnimalData where id < #{parameters.p1,jdbcType=INTEGER}");
    }
}
