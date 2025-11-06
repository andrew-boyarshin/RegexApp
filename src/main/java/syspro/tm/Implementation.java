package syspro.tm;

import java.util.ArrayList;
import java.util.List;

final class Implementation {
    public final RegexEngine engine;
    public final String engineName;
    public final List<JobResults> benchmarkResults = new ArrayList<>();

    Implementation(RegexEngine engine) {
        this.engine = engine;
        this.engineName = engine.getClass().getCanonicalName();
    }
}
