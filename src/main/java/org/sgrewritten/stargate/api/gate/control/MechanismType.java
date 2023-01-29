package org.sgrewritten.stargate.api.gate.control;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An enum containing the different types a portal position can have
 */
public class MechanismType {

    private static final List<MechanismType> values = new ArrayList<>();

    private @NotNull String name;

    private int ordinal;

    /**
     * A sign that works as an interface for selecting destination and displaying portal information
     */
    public static final MechanismType SIGN = new MechanismType("SIGN");

    /**
     * A button that works as an interface for confirming destination or activating a portal
     */
    public static final MechanismType BUTTON = new MechanismType("BUTTON");

    static {
        registerTypes();
    }

    public MechanismType(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    private static void registerTypes() {
        MechanismType[] types = {
                SIGN,
                BUTTON
        };
        for (MechanismType type : types) {
            registerMechanismType(type);
        }
    }

    public static void registerMechanismType(MechanismType type) {
        for (MechanismType mechanismType : MechanismType.values()) {
            if (type.name().equals(mechanismType.name())) {
                throw new IllegalArgumentException("A Mechanism type of name '" + type.name() + "' already exist");
            }
        }
        type.ordinal = values.size();
        values.add(type);
    }

    public static List<MechanismType> values() {
        return values;
    }

    public static MechanismType valueOf(String name) {
        // O(N) speed, as this "enum" is so damn small
        for (MechanismType type : values) {
            if (type.toString().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Could not find any registered mechanismtype of the name '" + name + "'");
    }

    @Override
    public String toString() {
        return name;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return ordinal;
    }
}
