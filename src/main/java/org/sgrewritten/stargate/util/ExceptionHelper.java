package org.sgrewritten.stargate.util;

public class ExceptionHelper {

    private ExceptionHelper(){
        throw new IllegalStateException("Utility class");
    }
    public static <T extends Throwable> boolean doesThrow(Class<T> expectedType, Runnable runnable) {
        try {
            runnable.run();
            return false;
        } catch (Exception actualException) {
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
        } catch (Exception exception) {
            return false;
        }
    }
}
