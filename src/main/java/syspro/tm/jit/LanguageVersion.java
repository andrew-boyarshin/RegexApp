package syspro.tm.jit;

import static syspro.tm.jit.Language.C;
import static syspro.tm.jit.Language.Cpp;

public final class LanguageVersion {
    public static final LanguageVersion Cpp98 = new LanguageVersion(Cpp, 98);
    public static final LanguageVersion Cpp03 = new LanguageVersion(Cpp, 3);
    public static final LanguageVersion Cpp11 = new LanguageVersion(Cpp, 11);
    public static final LanguageVersion Cpp14 = new LanguageVersion(Cpp, 14);
    public static final LanguageVersion Cpp17 = new LanguageVersion(Cpp, 17);
    public static final LanguageVersion Cpp20 = new LanguageVersion(Cpp, 20);
    public static final LanguageVersion Cpp23 = new LanguageVersion(Cpp, 23);
    public static final LanguageVersion Cpp26 = new LanguageVersion(Cpp, 26);
    public static final LanguageVersion C99 = new LanguageVersion(C, 99);
    public static final LanguageVersion C11 = new LanguageVersion(C, 11);
    public static final LanguageVersion C17 = new LanguageVersion(C, 17);

    public final Language language;
    public final int revision;
    public final boolean extensions;

    private LanguageVersion(Language language, int revision) {
        this(language, revision, false);
    }

    private LanguageVersion(Language language, int revision, boolean extensions) {
        this.language = language;
        this.revision = revision;
        this.extensions = extensions;
    }

    public LanguageVersion withExtensions() {
        return new LanguageVersion(language, revision, true);
    }

    @Override
    public String toString() {
        return language.toString() + revision + (extensions ? " with extensions" : "");
    }
}
