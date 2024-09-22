package org.sgrewritten.stargate.util.portal;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.gate.GateMock;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;
import org.sgrewritten.stargate.api.network.portal.behavior.PortalBehavior;

import java.util.List;
import java.util.UUID;

public class PortalMock implements RealPortal {
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
    public UUID getWorldUuid() {
        return null;
    }

    @Override
    public void close(boolean forceClose) {

    }

    @Override
    public void open(Player player) {

    }

    @Override
    public String getName() {
        return null;
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
    public void setOwner(UUID targetPlayer) {

    }

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return false;
    }

    @Override
    public void addFlag(PortalFlag flag) {

    }

    @Override
    public void removeFlag(PortalFlag flag) {

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
    public void updateState() {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public GlobalPortalId getGlobalId() {
        return null;
    }

    @Override
    public StorageType getStorageType() {
        return null;
    }

    @Override
    public void setName(String newName) {

    }

    @Override
    public void activate(Player player) {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void doTeleport(@NotNull Entity target, @Nullable Portal destination) {

    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public void open(@Nullable Portal destination, @Nullable Player actor) {

    }

    @Override
    public void setSignColor(DyeColor color, PortalPosition portalPosition) {

    }

    @Override
    public GateAPI getGate() {
        return new GateMock();
    }

    @Override
    public void close(long relatedOpenTime) {

    }

    @Override
    public Location getExit() {
        return null;
    }

    @Override
    public List<Location> getPortalPosition(PositionType type) {
        return null;
    }

    @Override
    public UUID getActivatorUUID() {
        return null;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void setMetadata(String data) {

    }

    @Override
    public String getMetadata() {
        return null;
    }

    @Override
    public BlockFace getExitFacing() {
        return null;
    }

    @Override
    public PortalBehavior getBehavior() {
        return null;
    }

    @Override
    public void setBehavior(PortalBehavior portalBehavior) {

    }

    @Override
    public void redrawSigns() {

    }
}
