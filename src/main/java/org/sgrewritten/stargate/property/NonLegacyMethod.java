package org.sgrewritten.stargate.property;

import org.sgrewritten.stargate.Stargate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * An enum containing various new features that might be available
 */
public enum NonLegacyMethod {

    /**
     * The powered minecart pushX method
     *
     * <p>This was added to Paper to change a powered minecart's x-push</p>
     */
    PUSH_X(NonLegacyClass.POWERED_MINECART, "setPushX", double.class),

    /**
     * The powered minecart pushZ method
     *
     * <p>This was added to Paper to change a powered minecart's z-push</p>
     */
    PUSH_Z(NonLegacyClass.POWERED_MINECART, "setPushZ", double.class),

    /**
     * The world get getMinHeight() method
     *
     * <p> This was added as a cause of the cave update </p>
     */
    GET_WORLD_MIN(NonLegacyClass.WORLD, "getMinHeight"),

    /**
     * The world get getMaxHeight() method
     *
     * <p> This was added as a cause of the cave update </p>
     */
    GET_WORLD_MAX(NonLegacyClass.WORLD, "getMaxHeight"),

    /**
     * The powered minecart getFuel method
     *
     * <p>This was added to Paper to change a powered minecart's z-push</p>
     */
    GET_FUEL(NonLegacyClass.POWERED_MINECART, "getFuel");


    private NonLegacyClass nonLegacyClass;
    private String methodInClassToCheckFor;
    private Class<?>[] parameters;
    private boolean isImplemented;

    /**
     * Instantiates a new non-legacy method
     *
     * @param classToCheckFor         <p>The class containing the method</p>
     * @param methodInClassToCheckFor <p>The legacy method itself</p>
     * @param parameterTypes          <p>The parameters expected by the method</p>
     */
    NonLegacyMethod(NonLegacyClass nonLegacyClass, String methodInClassToCheckFor, Class<?>... parameterTypes) {
        try {
            Class<?> aClass = nonLegacyClass.getRelatedClass();
            aClass.getMethod(methodInClassToCheckFor, parameterTypes);
            this.nonLegacyClass = nonLegacyClass;
            this.methodInClassToCheckFor = methodInClassToCheckFor;
            this.parameters = parameterTypes;
            isImplemented = true;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            isImplemented = false;
        }
    }

    /**
     * Checks whether this non-legacy method is available
     *
     * @return <p>Whether this non-legacy method is available</p>
     */
    public boolean isImplemented() {
        return isImplemented;
    }

    /**
     * Invokes this non-legacy method
     *
     * @param object     <p>The object to invoke the method on</p>
     * @param parameters <p>The parameters required for the method</p>
     * @return <p>The return value of the invocation</p>
     */
    @SuppressWarnings("UnusedReturnValue")
    public Object invoke(Object object, Object... parameters) {
        try {
            Class<?> aClass = nonLegacyClass.getRelatedClass();
            Method method = aClass.getMethod(methodInClassToCheckFor, this.parameters);
            return method.invoke(object, parameters);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            Stargate.log(Level.FINE, "Unable to invoke " + this.name());
            return null;
        }
    }

}
