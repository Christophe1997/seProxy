package chris.seProxy.security;

public enum Property {
    RANDOM(0), EQUALITY(1), ORDER(2), LIKE(3);

    int level;

    private Property(int level) {
        this.level = level;
    }

}
