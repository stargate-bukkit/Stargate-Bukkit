package org.sgrewritten.stargate.api.network.portal.behavior;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.event.portal.StargateAccessPortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateOpenPortalEvent;
import org.sgrewritten.stargate.api.event.portal.message.MessageType;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLineType;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.network.portal.formatting.data.TextLineData;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.thread.task.StargateGlobalTask;
import org.sgrewritten.stargate.util.MessageUtils;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public abstract class AbstractPortalBehavior implements PortalBehavior {

    protected RealPortal portal;
    protected final LanguageManager languageManager;
    private static final Pattern INTERNAL_FLAG = Pattern.compile("\\d");

    protected AbstractPortalBehavior(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    @Override
    public void onButtonClick(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (portal.hasFlag(StargateFlag.IRON_DOOR) && event.useInteractedBlock() == Event.Result.DENY) {
            GateAPI gate = portal.getGate();
            Block exitBlock = gate.getExit().add(gate.getFacing().getDirection()).getBlock();
            if (exitBlock.getType() == Material.IRON_DOOR) {
                BlockFace gateDirection = gate.getFacing();
                Directional doorDirection = (Directional) exitBlock.getBlockData();
                if (gateDirection == doorDirection.getFacing()) {
                    return;
                }
            }
        }

        Portal selectedDestination = getDestination();
        String message = null;
        MessageType messageType = null;
        boolean cancelled = false;

        if (selectedDestination == null) {
            message = languageManager.getErrorMessage(TranslatableMessage.INVALID);
            messageType = MessageType.DESTINATION_EMPTY;
            cancelled = true;
        }
        StargatePermissionManager permissionManager = new StargatePermissionManager(player, languageManager);
        if (!permissionManager.hasOpenPermissions(portal, selectedDestination)) {
            message = permissionManager.getDenyMessage();
            messageType = MessageType.DENY;
            cancelled = true;
        }
        StargateOpenPortalEvent stargateOpenEvent = new StargateOpenPortalEvent(player, portal, selectedDestination, cancelled, false);
        Bukkit.getPluginManager().callEvent(stargateOpenEvent);

        if (stargateOpenEvent.isCancelled()) {
            if (message != null) {
                MessageUtils.sendMessageFromPortal(portal, player, message, messageType);
            }
            return;
        }
        portal.open(stargateOpenEvent.getDestination(), player);
    }

    @Override
    public void onSignClick(@NotNull PlayerInteractEvent event) {
        if ((portal.getActivatorUUID() != null && !event.getPlayer().getUniqueId().equals(portal.getActivatorUUID()))) {
            return;
        }

        if (!event.getPlayer().isSneaking()) {
            return;
        }
        PermissionManager permissionManager = new StargatePermissionManager(event.getPlayer(), languageManager);
        StargateAccessPortalEvent accessEvent = new StargateAccessPortalEvent(event.getPlayer(), portal, !permissionManager.hasAccessPermission(portal),
                permissionManager.getDenyMessage());
        Bukkit.getPluginManager().callEvent(accessEvent);
        if (accessEvent.getDeny()) {
            MessageUtils.sendMessageFromPortal(portal, event.getPlayer(), accessEvent.getDenyReason(), MessageType.DENY);
            return;
        }
        UUID ownerUUID = portal.getOwnerUUID();
        LineData[] lineData = new LineData[]{
                new TextLineData(languageManager.getString(TranslatableMessage.PREFIX).trim(), SignLineType.TEXT),
                new TextLineData(languageManager.getString(TranslatableMessage.GATE_OWNED_BY).trim(), SignLineType.TEXT),
                new TextLineData(Bukkit.getOfflinePlayer(ownerUUID).getName(), SignLineType.TEXT),
                new TextLineData(INTERNAL_FLAG.matcher(portal.getAllFlagsString()).replaceAll(""), SignLineType.TEXT)
        };
        new StargateGlobalTask() {
            @Override
            public void run() {
                portal.getGate().drawControlMechanisms(lineData);
            }
        }.runNow();
        portal.activate(event.getPlayer());
    }

    @Override
    public void onDestroy() {
        LineData[] lineData = new LineData[]{
                new TextLineData(portal.getName(), SignLineType.TEXT),
                new TextLineData(),
                new TextLineData(),
                new TextLineData()
        };
        portal.getGate().drawControlMechanisms(lineData);
    }

    @Override
    public void assignPortal(@NotNull RealPortal portal) {
        this.portal = Objects.requireNonNull(portal);
    }

    @Override
    public @Nullable String getDestinationName() {
        return this.getDestination() == null ? null : this.getDestination().getName();
    }
}
