package org.sgrewritten.stargate.util;

public class ClassConditionsHelper {


    public static void assertInstanceOf(Class<?> clazz, Object object) {
        if (!clazz.isInstance(object)) {
            throw new IllegalArgumentException("Expected object to be instanceof '" + clazz.getName() + "', but was '" + object.getClass().getName() + "'");
        }
    }
}
