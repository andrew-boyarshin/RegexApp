import syspro.tm.RegexEngine;
import syspro.tm.test.JavaCachingRegexEngine;
import syspro.tm.test.JavaRegexEngine;

open module syspro.tm.RegexApp.test {
    requires org.junit.jupiter.api;
    requires syspro.tm.RegexApp;
    requires org.junit.jupiter.params;
    provides RegexEngine with JavaRegexEngine, JavaCachingRegexEngine;
}