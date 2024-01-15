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
     * The bungee ChatColor of() method
     *
     * <p>This was added to Spigot to allow RGB coloring</p>
     */
    CHAT_COLOR("net.md_5.bungee.api.ChatColor", "of", String.class),

    /**
     * The powered minecart pushX method
     *
     * <p>This was added to Paper to change a powered minecart's x-push</p>
     */
    PUSH_X("org.bukkit.entity.minecart.PoweredMinecart", "setPushX", double.class),

    /**
     * The powered minecart pushZ method
     *
     * <p>This was added to Paper to change a powered minecart's z-push</p>
     */
    PUSH_Z("org.bukkit.entity.minecart.PoweredMinecart", "setPushZ", double.class),

    /**
     * The world get getMinHeight() method
     *
     * <p> This was added as a cause of the cave update </p>
     */
    GET_WORLD_MIN("org.bukkit.World", "getMinHeight"),

    /**
     * The world get getMaxHeight() method
     *
     * <p> This was added as a cause of the cave update </p>
     */
    GET_WORLD_MAX("org.bukkit.World", "getMaxHeight"),

    /**
     * The paper PlayerAdvancementCriterionGrantEvent getPlayer() method
     *
     * <p> PlayerAdvancementCriterionGrantEvent was added to enable cancelling advancements </p>
     */
    PLAYER_ADVANCEMENT_CRITERION_EVENT("com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent", "getPlayer"),

    /**
     * The paper EntityInsideBlockEven getBlock() method
     *
     * <p> EntityInsideBlockEvent was added to allow cancelling certain blocks from getting triggered by entities </p>
     */
    ENTITY_INSIDE_BLOCK_EVENT("io.papermc.paper.event.entity.EntityInsideBlockEvent", "getBlock"),

    /**
     * The powered minecart getFuel method
     *
     * <p>This was added to Paper to change a powered minecart's z-push</p>
     */
    GET_FUEL("org.bukkit.entity.minecart.PoweredMinecart", "getFuel"),

    /**
     * The adventure text API
     * 
     * <p>Used to handle modern in-game text and its colouring</p>
     */
    COMPONENT("net.kyori.adventure.text.Component", "equals", Object.class),

    /**
     * Methods used to deal with double sided signs signs
     * 
     * <p>This was added as part of the 1.20 sign update
     */
    TWO_SIDED_SIGNS(" org.bukkit.block.sign.SignSide", "equals", Object.class),
    
    /**
     * Checks for folia
     * 
     * <p>An experiment from the PaperMC project that adds regional scheduling</p>
     */
    FOLIA("io.papermc.threadedregions.scheduler.EntityScheduler", "equals", Object.class);

    private String classToCheckFor;
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
    NonLegacyMethod(String classToCheckFor, String methodInClassToCheckFor, Class<?>... parameterTypes) {
        try {
            Class<?> aClass = Class.forName(classToCheckFor);
            aClass.getMethod(methodInClassToCheckFor, parameterTypes);
            this.classToCheckFor = classToCheckFor;
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
            Class<?> aClass = Class.forName(classToCheckFor);
            Method method = aClass.getMethod(methodInClassToCheckFor, this.parameters);
            return method.invoke(object, parameters);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            Stargate.log(Level.FINE, "Unable to invoke " + this.name());
            return null;
        }
    }

}
