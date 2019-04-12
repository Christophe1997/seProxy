package chris.seProxy.proxy.db;

import lombok.*;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private String tableCatalog;
    private String tableSchema;
    private String tableName;
    private List<Column> columns;

    public void addColumn(Column column) {
        columns.add(column);
    }

    public Optional<Column> getColumn(int ordinalPosition) {
        if (ordinalPosition >= columns.size()) {
            return Optional.empty();
        } else {
            return Optional.of(columns.get(ordinalPosition));
        }
    }
}
