package chris.seProxy.security;

import org.jetbrains.annotations.Contract;

public enum Level {
    RANDOM("RANDOM"), EQUALITY("EQUALITY"), ORDER("ORDER"), LIKE("LIKE");

    String level;

    @Contract(pure = true)
    Level(String level) {
    }

}
