package syspro.tm.jit;

public enum Language {
    Cpp, C;

    @Override
    public String toString() {
        return switch (this) {
            case Cpp -> "C++";
            case C -> "C";
        };
    }
}
