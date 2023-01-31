package org.sgrewritten.stargate.gate.control;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.DelayedAction;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.gate.control.GateActivationHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.util.ButtonHelper;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ButtonControlMechanism extends GatePosition implements GateActivationHandler {
    private final @NotNull GateAPI gate;
    private long openTime;
    private final int OPEN_DELAY = 20;
    protected UUID activator;

    public ButtonControlMechanism(@NotNull BlockVector positionLocation, GateAPI gate) {
        super(positionLocation);
        this.gate = Objects.requireNonNull(gate);
        this.drawButton(ButtonHelper.getButtonMaterial(gate.getFormat().getIrisMaterial(false)), gate.getFacing());
    }

    @Override
    public boolean isActive() {
        return openTime + OPEN_DELAY > System.currentTimeMillis();
    }

    @Override
    public @Nullable UUID getActivator() {
        return activator;
    }

    @Override
    public boolean onBlockClick(PlayerInteractEvent event, RealPortal portal) {
        portal.onButtonClick(event);
        event.setUseInteractedBlock(Event.Result.DENY);
        long openTime = System.currentTimeMillis();
        this.openTime = openTime;

        Stargate.addSynchronousSecAction(new DelayedAction(OPEN_DELAY, () -> {
            closePortal(portal, openTime);
            return true;
        }));
        return true;
    }

    /**
     * Closes the specified portal
     *
     * <p>Everytime most of the portals opens, there is going to be a scheduled event to close it after a specific time.
     * If a player enters the portal before this, then it is going to close, but the scheduled close event is still
     * going to be there. And if the portal gets activated again, it is going to close prematurely, because of this
     * already scheduled event. Solution to avoid this is to assign an open-time for each scheduled close event and
     * only close if the related open time matches with the most recent time the portal was opened.</p>
     *
     * @param realPortal <p>The portal to close</p>
     * @param openTime   <p>The time the portal was opened</p>
     */
    private void closePortal(RealPortal realPortal, long openTime) {
        if (this.openTime == openTime) {
            realPortal.close(false);
        }
    }

    @Override
    public String getName() {
        return getType().name();
    }

    @Override
    public MechanismType getType() {
        return MechanismType.BUTTON;
    }

    /**
     * Draw the button this ButtonMechanism relates to, if there already exist a button,
     * then this will be skipped
     *
     * @param buttonMaterial <p>The material of the button to draw</p>
     * @param facing         <p>The facing of the button to draw</p>
     */
    private void drawButton(Material buttonMaterial, BlockFace facing) {
        Block button = gate.getLocation(positionLocation).getBlock();
        if (ButtonHelper.isButton(button.getType())) {
            return;
        }
        Stargate.log(Level.FINEST, "buttonMaterial: " + buttonMaterial);
        Directional buttonData = (Directional) Bukkit.createBlockData(buttonMaterial);
        buttonData.setFacing(facing);

        button.setBlockData(buttonData);
    }


}
