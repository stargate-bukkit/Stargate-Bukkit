package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.event.StargateCreateEvent;
import net.knarcraft.stargate.portal.property.PortalLocation;
import net.knarcraft.stargate.portal.property.PortalOption;
import net.knarcraft.stargate.portal.property.PortalOptions;
import net.knarcraft.stargate.portal.property.PortalOwner;
import net.knarcraft.stargate.portal.property.gate.Gate;
import net.knarcraft.stargate.portal.property.gate.GateHandler;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.PermissionHelper;
import net.knarcraft.stargate.utility.PortalFileHelper;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;
import java.util.Map;

/**
 * The portal creator can create and validate a new portal
 */
public class PortalCreator {

    private Portal portal;
    private final SignChangeEvent event;
    private final Player player;

    /**
     * Instantiates a new portal creator
     *
     * @param event  <p>The sign change event which initialized the creation</p>
     * @param player <p>The player creating the portal</p>
     */
    public PortalCreator(SignChangeEvent event, Player player) {
        this.event = event;
        this.player = player;
    }

    /**
     * Creates a new portal
     *
     * @return <p>The created portal</p>
     */
    public Portal createPortal() {
        BlockLocation signLocation = new BlockLocation(event.getBlock());
        Block signControlBlock = signLocation.getParent();

        //Return early if the sign is not placed on a block, or the block is not a control block
        if (signControlBlock == null || GateHandler.getGatesByControlBlock(signControlBlock).length == 0) {
            Stargate.debug("createPortal", "Control block not registered");
            return null;
        }

        //The control block is already part of another portal
        if (PortalHandler.getByBlock(signControlBlock) != null) {
            Stargate.debug("createPortal", "idParent belongs to existing stargate");
            return null;
        }

        //Get necessary information from the gate's sign
        String portalName = PortalHandler.filterName(event.getLine(0));
        String destinationName = PortalHandler.filterName(event.getLine(1));
        String network = PortalHandler.filterName(event.getLine(2));
        String options = PortalHandler.filterName(event.getLine(3)).toLowerCase();

        //Get portal options available to the player creating the portal
        Map<PortalOption, Boolean> portalOptions = PortalHandler.getPortalOptions(player, destinationName, options);

        //Get the yaw
        float yaw = DirectionHelper.getYawFromLocationDifference(signControlBlock.getLocation(),
                signLocation.getLocation());

        //Get the direction the button should be facing
        BlockFace buttonFacing = DirectionHelper.getBlockFaceFromYaw(yaw);

        PortalLocation portalLocation = new PortalLocation();
        portalLocation.setButtonFacing(buttonFacing).setYaw(yaw).setSignLocation(signLocation);

        Stargate.debug("createPortal", "Finished getting all portal info");

        //Try and find a gate matching the new portal
        Gate gate = PortalHandler.findMatchingGate(portalLocation, player.getWorld());
        if ((gate == null) || (portalLocation.getButtonVector() == null)) {
            Stargate.debug("createPortal", "Could not find matching gate layout");
            return null;
        }

        //If the portal is a bungee portal and invalid, abort here
        if (!PortalHandler.isValidBungeePortal(portalOptions, player, destinationName, network)) {
            Stargate.debug("createPortal", "Portal is an invalid bungee portal");
            return null;
        }

        //Debug
        StringBuilder builder = new StringBuilder();
        for (PortalOption option : portalOptions.keySet()) {
            builder.append(option.getCharacterRepresentation()).append(" = ").append(portalOptions.get(option)).append(" ");
        }
        Stargate.debug("createPortal", builder.toString());

        //Use default network if a proper alternative is not set
        if (!portalOptions.get(PortalOption.BUNGEE) && (network.length() < 1 || network.length() > 11)) {
            network = Stargate.getDefaultNetwork();
        }

        boolean deny = false;
        String denyMessage = "";

        //Check if the player can create portals on this network. If not, create a personal portal
        if (!portalOptions.get(PortalOption.BUNGEE) && !PermissionHelper.canCreateNetworkGate(player, network)) {
            Stargate.debug("createPortal", "Player doesn't have create permissions on network. Trying personal");
            if (PermissionHelper.canCreatePersonalPortal(player)) {
                network = player.getName();
                if (network.length() > 11) {
                    network = network.substring(0, 11);
                }
                Stargate.debug("createPortal", "Creating personal portal");
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("createPersonal"));
            } else {
                Stargate.debug("createPortal", "Player does not have access to network");
                deny = true;
                denyMessage = Stargate.getString("createNetDeny");
            }
        }

        //Check if the player can create this gate layout
        String gateName = gate.getFilename();
        gateName = gateName.substring(0, gateName.indexOf('.'));
        if (!deny && !PermissionHelper.canCreatePortal(player, gateName)) {
            Stargate.debug("createPortal", "Player does not have access to gate layout");
            deny = true;
            denyMessage = Stargate.getString("createGateDeny");
        }

        //Check if the user can create portals to this world.
        if (!portalOptions.get(PortalOption.BUNGEE) && !deny && destinationName.length() > 0) {
            Portal portal = PortalHandler.getByName(destinationName, network);
            if (portal != null) {
                String world = portal.getWorld().getName();
                if (PermissionHelper.cannotAccessWorld(player, world)) {
                    Stargate.debug("canCreateNetworkGate", "Player does not have access to destination world");
                    deny = true;
                    denyMessage = Stargate.getString("createWorldDeny");
                }
            }
        }

        //Check if a conflict exists
        if (conflictsWithExistingPortal(gate, portalLocation.getTopLeft(), yaw, player)) {
            return null;
        }

        PortalOwner owner = new PortalOwner(player);
        this.portal = new Portal(portalLocation, null, destinationName, portalName, network, gate, owner,
                portalOptions);
        return validatePortal(denyMessage, event.getLines(), deny);
    }

    /**
     * Validates the newly created portal assigned to this portal validator
     *
     * @param denyMessage <p>The deny message to displayed if the creation has already been denied</p>
     * @param lines       <p>The lines on the sign causing the portal to be created</p>
     * @param deny        <p>Whether the portal creation has already been denied</p>
     * @return <p>The portal or null if its creation was denied</p>
     */
    public Portal validatePortal(String denyMessage, String[] lines, boolean deny) {
        PortalLocation portalLocation = portal.getLocation();
        Gate gate = portal.getStructure().getGate();
        PortalOptions portalOptions = portal.getOptions();
        String portalName = portal.getName();
        String destinationName = portal.getDestinationName();

        int createCost = Stargate.getEconomyConfig().getCreateCost(player, gate);

        //Call StargateCreateEvent to let other plugins cancel or overwrite denial
        StargateCreateEvent stargateCreateEvent = new StargateCreateEvent(player, portal, lines, deny,
                denyMessage, createCost);
        Stargate.getInstance().getServer().getPluginManager().callEvent(stargateCreateEvent);
        if (stargateCreateEvent.isCancelled()) {
            return null;
        }

        //Tell the user why it was denied from creating the portal
        if (stargateCreateEvent.getDeny()) {
            Stargate.getMessageSender().sendErrorMessage(player, stargateCreateEvent.getDenyReason());
            return null;
        }

        createCost = stargateCreateEvent.getCost();

        //Check if the new portal is valid
        if (!checkIfNewPortalIsValid(createCost, portalName)) {
            return null;
        }

        //Add button if the portal is not always on
        if (!portalOptions.isAlwaysOn()) {
            PortalFileHelper.generatePortalButton(portal, portalLocation.getButtonFacing());
        }

        //Register the new portal
        PortalHandler.registerPortal(portal);
        updateNewPortalOpenState(destinationName);

        //Update portals pointing at this one if it's not a bungee portal
        if (!portal.getOptions().isBungee()) {
            PortalHandler.updatePortalsPointingAtNewPortal(portal);
        }

        PortalFileHelper.saveAllPortals(portal.getWorld());

        return portal;
    }

    /**
     * Checks whether the newly created, but unregistered portal is valid
     *
     * @param cost       <p>The cost of creating the portal</p>
     * @param portalName <p>The name of the newly created portal</p>
     * @return <p>True if the portal is completely valid</p>
     */
    private boolean checkIfNewPortalIsValid(int cost, String portalName) {
        //Check if the portal name can fit on the sign with padding (>name<)
        if (portal.getName().length() < 1 || portal.getName().length() > 11) {
            Stargate.debug("createPortal", "Name length error");
            Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("createNameLength"));
            return false;
        }

        if (portal.getOptions().isBungee()) {
            //Check if the bungee portal's name has been duplicated
            if (PortalHandler.getBungeePortals().get(portal.getName().toLowerCase()) != null) {
                Stargate.debug("createPortal::Bungee", "Gate name duplicate");
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("createExists"));
                return false;
            }
        } else {
            //Check if the portal name has been duplicated on the network
            if (PortalHandler.getByName(portal.getName(), portal.getNetwork()) != null) {
                Stargate.debug("createPortal", "Gate name duplicate");
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("createExists"));
                return false;
            }

            //Check if the number of portals in the network has been surpassed
            List<String> networkList = PortalHandler.getAllPortalNetworks().get(portal.getNetwork().toLowerCase());
            int maxGates = Stargate.getGateConfig().maxGatesEachNetwork();
            if (maxGates > 0 && networkList != null && networkList.size() >= maxGates) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("createFull"));
                return false;
            }
        }

        if (cost > 0) {
            //Deduct the required fee from the player
            if (!Stargate.getEconomyConfig().chargePlayerIfNecessary(player, cost)) {
                EconomyHelper.sendInsufficientFundsMessage(portalName, player, cost);
                Stargate.debug("createPortal", "Insufficient Funds");
                return false;
            } else {
                EconomyHelper.sendDeductMessage(portalName, player, cost);
            }
        }
        return true;
    }

    /**
     * Updates the open state of the newly created portal
     *
     * @param destinationName <p>The name of the destination portal. Only used if set as always on</p>
     */
    private void updateNewPortalOpenState(String destinationName) {
        portal.drawSign();
        if (portal.getOptions().isRandom() || portal.getOptions().isBungee()) {
            //Open the implicitly always on portal
            portal.getPortalOpener().openPortal(true);
        } else if (portal.getOptions().isAlwaysOn()) {
            //For a normal always-on portal, open both the portal and the destination
            Portal destinationPortal = PortalHandler.getByName(destinationName, portal.getNetwork());
            if (destinationPortal != null) {
                portal.getPortalOpener().openPortal(true);
                destinationPortal.drawSign();
            }
        } else {
            //Update the block type for the portal's opening to the closed block as the closed block can be anything,
            // not just air or water
            for (BlockLocation entrance : portal.getStructure().getEntrances()) {
                entrance.setType(portal.getGate().getPortalClosedBlock());
            }
        }
    }

    /**
     * Checks whether the new portal conflicts with an existing portal
     *
     * @param gate    <p>The gate type of the new portal</p>
     * @param topLeft <p>The top-left block of the new portal</p>
     * @param yaw     <p>The yaw when looking directly outwards from the portal</p>
     * @param player  <p>The player creating the new portal</p>
     * @return <p>True if a conflict was found. False otherwise</p>
     */
    private static boolean conflictsWithExistingPortal(Gate gate, BlockLocation topLeft, double yaw, Player player) {
        for (RelativeBlockVector borderVector : gate.getLayout().getBorder()) {
            BlockLocation borderBlockLocation = topLeft.getRelativeLocation(borderVector, yaw);
            if (PortalHandler.getByBlock(borderBlockLocation.getBlock()) != null) {
                Stargate.debug("createPortal", "Gate conflicts with existing gate");
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("createConflict"));
                return true;
            }
        }
        return false;
    }

}
