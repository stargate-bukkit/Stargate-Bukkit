package org.sgrewritten.stargate.api.network.portal.flag;

import org.jetbrains.annotations.ApiStatus;
import org.sgrewritten.stargate.util.ExceptionHelper;

import java.util.HashMap;
import java.util.Map;

public class CustomFlag implements PortalFlag {

    private static final Map<Character, CustomFlag> existingFlags = new HashMap<>();
    private final char characterRepresentation;
    private boolean behaviorFlag;
    private boolean internalFlag;
    private boolean modified;

    private CustomFlag(char characterRepresentation) {
        if (Character.isLowerCase(characterRepresentation)) {
            throw new IllegalArgumentException("Character can't be lowercase");
        }
        if(ExceptionHelper.doesNotThrow(() -> StargateFlag.valueOf(characterRepresentation))){
            throw new IllegalArgumentException("Flag is conflicting with Stargate core");
        }
        this.characterRepresentation = characterRepresentation;
    }

    /**
     * Clear all registered flags
     */
    @ApiStatus.Internal
    static void clear() {
        existingFlags.clear();
    }

    @Override
    public boolean isBehaviorFlag() {
        return behaviorFlag;
    }

    @Override
    public boolean isInternalFlag() {
        return internalFlag;
    }

    /**
     *
     * @param internalFlag <p>Whether this flag should be hidden for the user</p>
     * @param behaviorFlag <p>Whether this is a selector/behavior flag</p>
     * @return <p>This flag</p>
     */
    public CustomFlag modify(boolean internalFlag, boolean behaviorFlag){
        if(this.modified){
            throw new IllegalStateException("Flag has already been modified by another plugin");
        }
        this.modified = true;
        this.internalFlag = internalFlag;
        this.behaviorFlag = behaviorFlag;
        return this;
    }

    @Override
    public char getCharacterRepresentation() {
        return characterRepresentation;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public String toString(){
        return "CustomFlag(" + characterRepresentation + ")";
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof CustomFlag otherFlag)){
            return false;
        }
        return otherFlag.getCharacterRepresentation() == this.getCharacterRepresentation();
    }

    @Override
    public int hashCode(){
        return characterRepresentation;
    }

    /**
     * Get or create flags
     * @param characterRepresentation <p>The character representing the flag to get</p>
     * @return <p>The custom flag</p>
     */
    public static CustomFlag getOrCreate(char characterRepresentation) {
        return existingFlags.computeIfAbsent(characterRepresentation, CustomFlag::new);
    }
}
