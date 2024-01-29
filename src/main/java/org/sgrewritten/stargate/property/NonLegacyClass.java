package org.sgrewritten.stargate.property;

public enum NonLegacyClass {

    CHAT_COLOR("net.md_5.bungee.api.ChatColor"),
    POWERED_MINECART("org.bukkit.entity.minecart.PoweredMinecart"),
    WORLD("org.bukkit.World"),
    PLAYER_ADVANCEMENT_CRITERION_EVENT("com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent"),
    ENTITY_INSIDE_BLOCK_EVENT("io.papermc.paper.event.entity.EntityInsideBlockEvent"),
    COMPONENT("net.kyori.adventure.text.Component"),
    SIGN_SIDE("org.bukkit.block.sign.SignSide"),
    /**
     * Check for Folia compatibility
     */
    REGIONIZED_SERVER("io.papermc.paper.threadedregions.RegionizedServer"),
    /**
     * Check for bkcommonlib compatibility
     */
    MULTI_BLOCK_CHANGE_EVENT("com.bergerkiller.bukkit.common.events.MultiBlockChangeEvent");


    private Class<?> aClass = null;
    private boolean isImplemented;
    NonLegacyClass(String classToCheckFor){
        try {
            this.aClass = Class.forName(classToCheckFor);
            isImplemented = true;
        } catch (ClassNotFoundException ignored) {
            isImplemented = false;
        }
    }

    public boolean isImplemented() {
        return isImplemented;
    }

    public Class<?> getRelatedClass() throws ClassNotFoundException {
        if(aClass == null){
            throw new ClassNotFoundException();
        }
        return aClass;
    }
}
