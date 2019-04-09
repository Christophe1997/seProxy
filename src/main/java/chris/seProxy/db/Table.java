package chris.seProxy.db;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
public class Table {
    @Getter @Setter
    private String tableCatalog;
    @Getter @Setter
    private String tableSchema;
    @Getter @Setter
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
