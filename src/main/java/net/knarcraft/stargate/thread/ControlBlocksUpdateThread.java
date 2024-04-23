package net.knarcraft.stargate.thread;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.ControlBlockUpdateRequest;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.MaterialHelper;
import net.knarcraft.stargate.utility.PortalFileHelper;
import org.bukkit.Material;

/**
 * This thread updates the signs and buttons of Stargates, if deemed necessary
 */
public class ControlBlocksUpdateThread implements Runnable {

    @Override
    public void run() {
        //Abort if there's no work to be done
        ControlBlockUpdateRequest controlBlockUpdateRequest = Stargate.getButtonUpdateRequestQueue().poll();
        if (controlBlockUpdateRequest == null) {
            return;
        }

        Portal portal = controlBlockUpdateRequest.portal();
        portal.drawSign();

        BlockLocation buttonLocation = PortalFileHelper.getButtonLocation(portal);
        if (buttonLocation == null) {
            return;
        }

        Stargate.debug("ControlBlocksUpdateThread", "Updating control blocks for portal " + portal);

        if (portal.getOptions().isAlwaysOn()) {
            //Clear button if it exists
            if (MaterialHelper.isButtonCompatible(buttonLocation.getType())) {
                Material newMaterial = PortalFileHelper.decideRemovalMaterial(buttonLocation, portal);
                Stargate.addControlBlockUpdateRequest(new BlockChangeRequest(buttonLocation, newMaterial, null));
            }
        } else {
            //Replace button if the material is not a button
            if (!MaterialHelper.isButtonCompatible(buttonLocation.getType())) {
                PortalFileHelper.generatePortalButton(portal, DirectionHelper.getBlockFaceFromYaw(portal.getYaw()));
            }
        }
    }

}
