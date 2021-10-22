package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.event.StargateCreateEvent;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EconomyHandler;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.PermissionHelper;
import net.knarcraft.stargate.utility.PortalFileHelper;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
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
        Block idParent = signLocation.getParent();

        //Return early if the sign is not placed on a block, or the block is not a control block
        if (idParent == null || GateHandler.getGatesByControlBlock(idParent).length == 0) {
            Stargate.debug("createPortal", "Control block not registered");
            return null;
        }

        //The control block is already part of another portal
        if (PortalHandler.getByBlock(idParent) != null) {
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
        float yaw = DirectionHelper.getYawFromLocationDifference(idParent.getLocation(), signLocation.getLocation());

        //Get the direction the button should be facing
        BlockFace buttonFacing = DirectionHelper.getBlockFaceFromYaw(yaw);

        PortalLocation portalLocation = new PortalLocation();
        portalLocation.setButtonFacing(buttonFacing).setYaw(yaw).setSignLocation(signLocation);

        Stargate.debug("createPortal", "Finished getting all portal info");

        //Try and find a gate matching the new portal
        Gate gate = PortalHandler.findMatchingGate(portalLocation, player);
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
            if (PermissionHelper.canCreatePersonalGate(player)) {
                network = player.getName();
                if (network.length() > 11) {
                    network = network.substring(0, 11);
                }
                Stargate.debug("createPortal", "Creating personal portal");
                Stargate.sendErrorMessage(player, Stargate.getString("createPersonal"));
            } else {
                Stargate.debug("createPortal", "Player does not have access to network");
                deny = true;
                denyMessage = Stargate.getString("createNetDeny");
            }
        }

        //Check if the player can create this gate layout
        String gateName = gate.getFilename();
        gateName = gateName.substring(0, gateName.indexOf('.'));
        if (!deny && !PermissionHelper.canCreateGate(player, gateName)) {
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
        if (PortalHandler.conflictsWithExistingPortal(gate, portalLocation.getTopLeft(), yaw, player)) {
            return null;
        }

        this.portal = new Portal(portalLocation, null, destinationName, portalName,
                network, gate, player.getUniqueId(), player.getName(), portalOptions);
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

        int createCost = EconomyHandler.getCreateCost(player, gate);

        //Call StargateCreateEvent to let other plugins cancel or overwrite denial
        StargateCreateEvent stargateCreateEvent = new StargateCreateEvent(player, portal, lines, deny,
                denyMessage, createCost);
        Stargate.server.getPluginManager().callEvent(stargateCreateEvent);
        if (stargateCreateEvent.isCancelled()) {
            return null;
        }

        //Tell the user why it was denied from creating the portal
        if (stargateCreateEvent.getDeny()) {
            Stargate.sendErrorMessage(player, stargateCreateEvent.getDenyReason());
            return null;
        }

        createCost = stargateCreateEvent.getCost();

        //Check if the new portal is valid
        if (!checkIfNewPortalIsValid(createCost, portalName)) {
            return null;
        }

        //Add button if the portal is not always on
        if (!portalOptions.isAlwaysOn()) {
            generatePortalButton(portalLocation.getTopLeft(), portalLocation.getButtonVector(),
                    portalLocation.getButtonFacing());
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
        // Name & Network can be changed in the event, so do these checks here.
        if (portal.getName().length() < 1 || portal.getName().length() > 11) {
            Stargate.debug("createPortal", "Name length error");
            Stargate.sendErrorMessage(player, Stargate.getString("createNameLength"));
            return false;
        }

        //Don't do network checks for bungee portals
        if (portal.getOptions().isBungee()) {
            if (PortalHandler.getBungeePortals().get(portal.getName().toLowerCase()) != null) {
                Stargate.debug("createPortal::Bungee", "Gate name duplicate");
                Stargate.sendErrorMessage(player, Stargate.getString("createExists"));
                return false;
            }
        } else {
            if (PortalHandler.getByName(portal.getName(), portal.getNetwork()) != null) {
                Stargate.debug("createPortal", "Gate name duplicate");
                Stargate.sendErrorMessage(player, Stargate.getString("createExists"));
                return false;
            }

            //Check if there are too many gates in this network
            List<String> networkList = PortalHandler.getAllPortalNetworks().get(portal.getNetwork().toLowerCase());
            if (Stargate.maxGatesEachNetwork > 0 && networkList != null && networkList.size() >= Stargate.maxGatesEachNetwork) {
                Stargate.sendErrorMessage(player, Stargate.getString("createFull"));
                return false;
            }
        }

        if (cost > 0) {
            if (!EconomyHandler.chargePlayerIfNecessary(player, cost)) {
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
     * Generates a button for a portal
     *
     * @param topLeft      <p>The top-left block of the portal</p>
     * @param buttonVector <p>A relative vector pointing at the button</p>
     * @param buttonFacing <p>The direction the button should be facing</p>
     */
    private void generatePortalButton(BlockLocation topLeft, RelativeBlockVector buttonVector,
                                      BlockFace buttonFacing) {
        //Go one block outwards to find the button's location rather than the control block's location
        BlockLocation button = topLeft.getRelativeLocation(buttonVector.addToVector(
                RelativeBlockVector.Property.DISTANCE, 1), portal.getYaw());

        Directional buttonData = (Directional) Bukkit.createBlockData(portal.getGate().getPortalButton());
        buttonData.setFacing(buttonFacing);
        button.getBlock().setBlockData(buttonData);
        portal.getStructure().setButton(button);
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

}
