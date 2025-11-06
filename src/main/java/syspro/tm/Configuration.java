package syspro.tm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

public final class Configuration {
    private static volatile List<Implementation> implementations;
    private static volatile List<TestData> testData;
    private static volatile List<TestData> testCases;
    private static volatile List<TestData> benchmarkCases;
    private static volatile ServiceLoader<ConfigurationProvider> configurationProviderLoader;

    private Configuration() {
    }

    public static Iterable<ConfigurationProvider> configurationProviders() {
        final var loader = configurationProviderLoader;
        if (loader == null) {
            return configurationProviderLoader = ServiceLoader.load(ConfigurationProvider.class);
        }
        return loader;
    }

    static List<Implementation> implementations() {
        final var list = implementations;
        if (list == null) {
            return implementations = List.copyOf(collectImplementations());
        }
        return list;
    }

    private static List<Implementation> collectImplementations() {
        final var result = new ArrayList<Implementation>();

        final var engineLoader = ServiceLoader.load(RegexEngine.class);

        engineLoop:
        for (final var engine : engineLoader) {
            for (final var configurationProvider : Configuration.configurationProviders()) {
                if (configurationProvider.shouldSkipEngine(engine)) {
                    continue engineLoop;
                }
            }
            result.add(new Implementation(engine));
        }

        return result;
    }

    static List<TestData> internalTestData() {
        final var list = testData;
        if (list == null) {
            return testData = List.copyOf(collectTestData());
        }
        return list;
    }

    public static List<TestData> testData() {
        final var list = internalTestData();
        final var size = list.size();
        final var result = new TestData[size];
        for (int i = 0; i < size; i++) {
            result[i] = list.get(i).publicClone();
        }
        return Arrays.asList(result);
    }

    private static List<TestData> collectTestData() {
        final var result = new ArrayList<TestData>();

        final var testCaseListBuilder = new TestDataListBuilderImpl(result);
        for (final var configurationProvider : Configuration.configurationProviders()) {
            try {
                testCaseListBuilder.currentProvider = configurationProvider;
                configurationProvider.provideTestData(testCaseListBuilder);
                while (testCaseListBuilder.currentGroup != null) {
                    testCaseListBuilder.currentGroup.close();
                }
            } finally {
                testCaseListBuilder.currentProvider = null;
            }
        }

        final var testCaseIterator = result.listIterator();
        while (testCaseIterator.hasNext()) {
            final var testCase = testCaseIterator.next();
            for (final var testCaseProvider : Configuration.configurationProviders()) {
                if (testCaseProvider.shouldSkipTestCase(testCase.provider, testCase.regex, testCase.input, testCase.benchmark)) {
                    testCaseIterator.remove();
                    break;
                }
            }
        }

        return result;
    }

    static List<TestData> testCases() {
        final var list = testCases;
        if (list == null) {
            return testCases = internalTestData().stream().filter(x -> !x.benchmark).toList();
        }
        return list;
    }

    static List<TestData> benchmarkCases() {
        final var list = benchmarkCases;
        if (list == null) {
            return benchmarkCases = internalTestData().stream().filter(x -> x.benchmark).toList();
        }
        return list;
    }

    static final class TestDataListBuilderImpl extends TestDataListBuilder {
        private final ArrayList<TestData> result;
        ConfigurationProvider currentProvider;

        public TestDataListBuilderImpl(ArrayList<TestData> result) {
            this.result = result;
        }

        @Override
        public void add(byte[] regex, byte[] input, boolean expected) {
            final var isBenchmark = currentGroup != null && currentGroup.isBenchmark;
            result.add(new TestData(currentProvider(), regex.clone(), input.clone(), expected, isBenchmark, null));
        }

        @Override
        public void addSlidingWindowBenchmark(byte[] regex, byte[] input, int inputWindow) {
            result.add(new TestData(currentProvider(), regex.clone(), input.clone(), true, true, inputWindow));
        }

        private ConfigurationProvider currentProvider() {
            if (currentProvider == null) {
                throw new IllegalStateException("Test cases are immutable at the moment.");
            }
            return currentProvider;
        }
    }
}
