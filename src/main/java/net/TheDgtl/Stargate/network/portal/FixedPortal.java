package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
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
    String destination;

    public FixedPortal(Network network, String name, String destiName, Block sign, EnumSet<PortalFlag> flags, UUID ownerUUID)
            throws NoFormatFound, GateConflict, NameError {
        super(network, name, sign, flags, ownerUUID);
        destination = destiName;

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
        lines[0] = NameSurround.PORTAL.getSurround(getColoredName(super.isLightSign()));
        lines[1] = NameSurround.DESTI.getSurround(loadDestination().getColoredName(super.isLightSign()));
        lines[2] = this.network.concatName();
        lines[3] = ((this.network.isPortalNameTaken(destination)) ? ""
                : Stargate.langManager.getString(LangMsg.DISCONNECTED));
        getGate().drawControll(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public IPortal loadDestination() {
        return this.network.getPortal(destination);
    }

    @Override
    public void close(boolean force) {
        super.close(force);
        this.openFor = null;
    }
}