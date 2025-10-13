package org.mybatis.dynamic.sql.select.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class WindowModel {
    private final List<BasicColumn> partitionByColumns = new ArrayList<>();
    private final List<BasicColumn> orderByColumns = new ArrayList<>();

    public WindowModel() {}

    private WindowModel(List<BasicColumn> partitionByColumns, List<BasicColumn> orderByColumns) {
        this.partitionByColumns.addAll(partitionByColumns);
        this.orderByColumns.addAll(orderByColumns);
    }

    public WindowModel partitionBy(BasicColumn column, BasicColumn...columns) {
        return new WindowModel(makeList(column, columns), orderByColumns);
    }

    public WindowModel orderBy(BasicColumn column, BasicColumn...columns) {
        return new WindowModel(partitionByColumns, makeList(column, columns));
    }

    public FragmentAndParameters render(RenderingContext renderingContext) {
        FragmentCollector fragmentCollector = new FragmentCollector();
        renderPartitionBy(renderingContext).ifPresent(fragmentCollector::add);
        renderOrderBy(renderingContext).ifPresent(fragmentCollector::add);

        return fragmentCollector.toFragmentAndParameters(
                Collectors.joining(" ", "over(", ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private Optional<FragmentAndParameters> renderPartitionBy(RenderingContext renderingContext) {
        return renderList(renderingContext, partitionByColumns, "partition by "); //$NON-NLS-1$
    }

    private Optional<FragmentAndParameters> renderOrderBy(RenderingContext renderingContext) {
        return renderList(renderingContext, orderByColumns, "order by "); //$NON-NLS-1$
    }

    private static Optional<FragmentAndParameters> renderList(RenderingContext renderingContext,
                                                              List<BasicColumn> columns, String prefix) {
        if (columns.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(columns.stream()
                    .map(bc -> bc.render(renderingContext))
                    .collect(FragmentCollector.collect())
                    .toFragmentAndParameters(Collectors.joining(", ", prefix, ""))); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static List<BasicColumn> makeList(BasicColumn column, BasicColumn... columns) {
        List<BasicColumn> list = new ArrayList<>();
        list.add(column);
        list.addAll(List.of(columns));
        return list;
    }
}
