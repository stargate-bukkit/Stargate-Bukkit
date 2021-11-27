package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.TranslatableMessage;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.EnumSet;
import java.util.UUID;

public class FixedPortal extends Portal {
    /**
     *
     */
    String destiName;

    public FixedPortal(Network network, String name, String destiName, Block sign, EnumSet<PortalFlag> flags, UUID ownerUUID)
            throws NoFormatFound, GateConflict, NameError {
        super(network, name, sign, flags, ownerUUID);
        this.destiName = destiName;

        drawControll();
    }


    /**
     * What will happen when a player clicks the sign?
     *
     * @param action
     * @param actor
     */
    @Override
    public void onSignClick(Action action, Player actor) {
    }

    @Override
    public void drawControll() {
        String[] lines = new String[4];
        lines[0] = super.colorDrawer.parseName(NameSurround.PORTAL, this);
        lines[2] = super.colorDrawer.parseLine(this.network.concatName());
        IPortal destination = loadDestination();
        if (destination != null)
            lines[1] = super.colorDrawer.parseName(NameSurround.DESTI, loadDestination());
        else {
            lines[1] = super.colorDrawer.parseLine(destiName);
            lines[3] = super.colorDrawer.parseError(Stargate.languageManager.getString(TranslatableMessage.DISCONNECTED), NameSurround.BUNGEE);
        }
        getGate().drawControll(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public IPortal loadDestination() {
        return this.network.getPortal(destiName);
    }

    @Override
    public void close(boolean force) {
        super.close(force);
        this.openFor = null;
    }
}