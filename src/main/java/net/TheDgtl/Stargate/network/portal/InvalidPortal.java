package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.network.Network;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A reference to a portal that does not at the moment exist, but still has a
 * name. For example a destination for a fixed portal that does not yet exist
 *
 * @author Thorin
 */
public class InvalidPortal implements Portal {

    final String name;

    public InvalidPortal(String name) {
        this.name = name;
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isOpenFor(Entity target) {
        return false;
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {
    }

    @Override
    public void doTeleport(Entity target) {
    }

    @Override
    public void close(boolean forceClose) {
    }

    @Override
    public void open(Player player) {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void overrideDestination(Portal destination) {
    }

    @Override
    public Network getNetwork() {
        return null;
    }

    @Override
    public void setNetwork(Network targetNetwork) {
    }

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return false;
    }

    @Override
    public String getAllFlagsString() {
        return null;
    }

    @Override
    public UUID getOwnerUUID() {
        return null;
    }

    @Override
    public void update() {
    }

    @Override
    public Portal loadDestination() {
        return null;
    }

    @Override
    public void setOwner(UUID targetPlayer) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
