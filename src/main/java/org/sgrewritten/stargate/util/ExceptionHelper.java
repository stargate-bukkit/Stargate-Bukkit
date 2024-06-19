package org.sgrewritten.stargate.util;

public class ExceptionHelper {

    private ExceptionHelper(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param expectedType <p>The expected type of the exception</p>
     * @param runnable <p>The function to be run</p>
     * @return <p>True if the exception got thrown</p>
     * @param <T> <p>Any throwable</p>
     */
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

    /**
     *
     * @param expectedType <p>The expected type to not throw</p>
     * @param runnable <p>A function to run</p>
     * @return <p>True if the expected type did not throw</p>
     * @param <T> <p>The expected type to not throw</p>
     */
    public static <T extends Throwable> boolean doesNotThrow(Class<T> expectedType, Runnable runnable) {
        return !doesThrow(expectedType, runnable);
    }

    /**
     * @param runnable <p>The runnable to check if it throws</p>
     * @return <p>True if runnable did not throw any exception</p>
     */
    public static boolean doesNotThrow(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
