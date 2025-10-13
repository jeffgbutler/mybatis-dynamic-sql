package org.mybatis.dynamic.sql.select.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class WindowModel {
    private final List<BasicColumn> partitionByColumns = new ArrayList<>();
    // TODO - convert to using OrderByModel and OrderByRenderer
    private final List<SortSpecification> orderByColumns = new ArrayList<>();

    public WindowModel() {}

    private WindowModel(List<BasicColumn> partitionByColumns, List<SortSpecification> orderByColumns) {
        this.partitionByColumns.addAll(partitionByColumns);
        this.orderByColumns.addAll(orderByColumns);
    }

    public WindowModel partitionBy(BasicColumn column, BasicColumn...columns) {
        return new WindowModel(makeList(column, columns), orderByColumns);
    }

    public WindowModel orderBy(SortSpecification column, SortSpecification...columns) {
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
        if (partitionByColumns.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(partitionByColumns.stream()
                    .map(bc -> bc.render(renderingContext))
                    .collect(FragmentCollector.collect())
                    .toFragmentAndParameters(Collectors.joining(", ", "partition by ", ""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private Optional<FragmentAndParameters> renderOrderBy(RenderingContext renderingContext) {
        if (orderByColumns.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(orderByColumns.stream()
                    .map(ss -> ss.renderForOrderBy(renderingContext))
                    .collect(FragmentCollector.collect())
                    .toFragmentAndParameters(Collectors.joining(", ", "order by ", ""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    @SafeVarargs
    private static <T> List<T> makeList(T column, T... columns) {
        List<T> list = new ArrayList<>();
        list.add(column);
        list.addAll(List.of(columns));
        return list;
    }
}
