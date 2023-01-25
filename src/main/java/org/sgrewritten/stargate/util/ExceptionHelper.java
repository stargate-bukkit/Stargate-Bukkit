package org.sgrewritten.stargate.util;

public class ExceptionHelper {
    public static <T extends Throwable> boolean doesThrow(Class<T> expectedType, Runnable runnable) {
        try {
            runnable.run();
            return false;
        } catch (Throwable actualException) {
            if (expectedType.isInstance(actualException)) {
                return true;
            } else {
                throw actualException;
            }
        }
    }

    public static <T extends Throwable> boolean doesNotThrow(Class<T> expectedType, Runnable runnable) {
        return !doesThrow(expectedType, runnable);
    }

    public static boolean doesNotThrow(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Throwable exception) {
            return false;
        }
    }
}
