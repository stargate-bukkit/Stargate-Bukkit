package org.sgrewritten.stargate.util.portal;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.format.SignLine;
import org.sgrewritten.stargate.gate.GateMock;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;

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
    public boolean hasFlag(Character flag) {
        return false;
    }

    @Override
    public void addFlag(Character flag) {

    }

    @Override
    public void removeFlag(Character flag) {

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
    public Portal getDestination() {
        return null;
    }

    @Override
    public String getDestinationName() {
        return null;
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
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public SignLine[] getDrawnControlLines() {
        return null;
    }

    @Override
    public void setSignColor(DyeColor color) {

    }

    @Override
    public void onButtonClick(PlayerInteractEvent event) {

    }

    @Override
    public void onSignClick(PlayerInteractEvent event) {

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
}
