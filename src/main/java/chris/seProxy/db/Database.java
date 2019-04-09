package chris.seProxy.db;

import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class Database {
    @Getter @Setter
    private String databaseName;

    private HashMap<String, Table> tableMap;

    public List<Table> tables() {
        return new ArrayList<>(tableMap.values());
    }

    public Optional<Table> getTable(String tableName) {
        return Optional.ofNullable(tableMap.get(tableName));
    }
}
