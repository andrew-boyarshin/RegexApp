import syspro.tm.*;

module syspro.tm.RegexApp {
    requires org.apache.commons.io;
    uses RegexEngine;
    uses ConfigurationProvider;
    provides ConfigurationProvider with DefaultTestCases;
    exports syspro.tm;
    exports syspro.tm.jit;
    exports syspro.tm.regex;
}