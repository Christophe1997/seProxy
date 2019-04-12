package chris.seProxy.proxy.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    private String columnName;
    private int ordinalPosition;
    private String columnDefault;
    private boolean isNullable;
    private String dataType;
    private String characterMaximumLength;
    private String characterOctetLength;
    private String numericPrecision;
    private String numericScale;
    private String datetimePrecision;
    private String characterSetName;
    private String collationName;
    private String columnType;
    private String columnKey;
    private String extra;
    private String privileges;
    private String columnComment;
    private String generationExpression;

    public boolean isVarchar() {
        return columnType.toUpperCase().contains("VARCHAR");
    }

    public boolean isInt() {
        return columnType.toUpperCase().contains("INT");
    }
}
