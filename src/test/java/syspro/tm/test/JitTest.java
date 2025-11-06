package syspro.tm.test;

import org.junit.jupiter.api.Test;
import syspro.tm.jit.LanguageVersion;
import syspro.tm.jit.NativeCompilerRunner;
import syspro.tm.jit.NativeLibraries;
import syspro.tm.jit.NativeLibrary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JitTest {
    @Test
    public void sumTest() {
        final var code = """
                #include <cstddef>
                
                #ifndef __has_attribute
                  #define __has_attribute(x) 0
                #endif
                
                #ifndef LIB_EXPORT
                  #if defined(_WIN32) || defined(_WIN64)
                    #define LIB_EXPORT    __declspec(dllexport)
                  #elif (defined(__GNUC__) && ((__GNUC__ > 4) || (__GNUC__ == 4) && (__GNUC_MINOR__ > 2))) || __has_attribute(visibility)
                    #ifdef ARM
                      #define LIB_EXPORT  __attribute__((externally_visible,visibility("default")))
                    #else
                      #define LIB_EXPORT  __attribute__((visibility("default")))
                    #endif
                  #else
                    #define LIB_EXPORT
                  #endif
                #endif
                
                extern "C" LIB_EXPORT int matches(char* input, std::size_t length)
                {
                    return length == 3 && input[0] + input[1] == input[2] ? 1 : 0;
                }
                """;
        final var dllLibrary = NativeCompilerRunner.compile(code, LanguageVersion.Cpp17);
        if (dllLibrary == null) {
            throw new RuntimeException("Failed to compile C++ source code:\n" + code);
        }
        NativeLibrary nativeLibrary;
        try {
            nativeLibrary = NativeLibraries.load(dllLibrary);
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("C++ library failed to load", exception);
        }
        nativeLibrary.setMatchesFunction("matches");
        assertTrue(nativeLibrary.callMatches(new byte[]{1, 2, 3}));
        assertFalse(nativeLibrary.callMatches(new byte[]{2, 2, 5}));
        assertFalse(nativeLibrary.callMatches(new byte[]{}));
        assertFalse(nativeLibrary.callMatches(new byte[]{'A'}));
    }
}
