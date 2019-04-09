package chris.seProxy.proxy.datasource;

import org.jetbrains.annotations.Contract;

public enum DClass {
    MYSQL("mysql"), ORACLE("oracle"), POSTGRESQL("postgresql"), SQLITE("sqlite");

    @Contract(pure = true)
    DClass(String dbName) {
    }

}
