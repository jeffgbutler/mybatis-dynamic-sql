package examples.r2dbc.spring;

import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class R2DBCUtils {
    public R2DBCUtils() {}

    public static Map<String, Object> rawMapper(Row row, RowMetadata rowMetadata) {
        List<? extends ColumnMetadata> columnMetadataList = rowMetadata.getColumnMetadatas();
        Map<String, Object> answer = new HashMap<>(columnMetadataList.size());

        for (ColumnMetadata columnMetadata : columnMetadataList) {
            String name = columnMetadata.getName();
            answer.put(name, row.get(name));
        }

        return answer;
    }
}
