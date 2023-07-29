package org.sgrewritten.stargate.network.portal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.api.PositionType;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.economy.StargateEconomyManagerMock;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;
import org.sgrewritten.stargate.util.LanguageManagerMock;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;

import java.util.*;

/**
 * A generator for generating fake portals
 */
public class PortalFactory {

    private final String portalDefaultName;
    private final String interPortalDefaultName;

    /**
     * Instantiates a new fake portal generator. When generating multiple portals,
     * Local portals will have a name based on "portal", and InterServer portals,
     * "iportal".
     */
    public PortalFactory() {
        this("portal", "iportal");
    }

    /**
     * Instantiates a new fake portal generator
     *
     * @param portalBaseName      <p>The base-name used for all fake portals</p>
     * @param interPortalBaseName <p>The base-name used for all fake inter-portals</p>
     */
    public PortalFactory(String portalBaseName, String interPortalBaseName) {
        this.portalDefaultName = portalBaseName;
        this.interPortalDefaultName = interPortalBaseName;
    }

    /**
     * Generates a map of fake portals
     *
     * @param world                    <p>The world the fake portal is located in</p>
     * @param portalNetwork            <p>The network of the generated portals</p>
     * @param createInterServerPortals <p>Whether to generate fake inter-server portals</p>
     * @param numberOfPortals          <p>The number of fake portals to generate</p>
     * @return <p>A map from the portal's name to the portal's object</p>
     * @throws InvalidStructureException <p>If an invalid structure is encountered</p>
     * @throws TranslatableException     <p>If the generated portal name is invalid</p>
     */
    public Map<String, RealPortal> generateFakePortals(World world, Network portalNetwork,
                                                       boolean createInterServerPortals, int numberOfPortals) throws
            InvalidStructureException, TranslatableException {
        Map<String, RealPortal> output = new HashMap<>();
        String baseName;
        if (createInterServerPortals) {
            baseName = interPortalDefaultName;
        } else {
            baseName = portalDefaultName;
        }

        for (int portalNumber = 0; portalNumber < numberOfPortals; portalNumber++) {
            String name = baseName + portalNumber;
            RealPortal portal = generateFakePortal(world, portalNetwork, name, createInterServerPortals);
            output.put(portal.getName(), portal);
        }
        return output;
    }

    /**
     * Generates a fake portal
     *
     * @param world                   <p>The world the fake portal is located in</p>
     * @param portalNetwork           <p>The network of the generated portal</p>
     * @param name                    <p>The name of the generated portal</p>
     * @param createInterServerPortal <p>Whether to generate a fake inter-server portal</p>
     * @return <p>A fake portal</p>
     * @throws InvalidStructureException <p>If an invalid structure is encountered</p>
     * @throws TranslatableException     <p>If the given portal name is invalid</p>
     */
    public static RealPortal generateFakePortal(World world, Network portalNetwork, String name, boolean createInterServerPortal)
            throws InvalidStructureException, TranslatableException {
        Set<PortalFlag> flags = generateRandomFlags();
        //To avoid using the Portal#open method on constructor, which uses an unimplemented function in MockBukkit (block-states)
        flags.remove(PortalFlag.ALWAYS_ON);
        Location topLeft = new Location(world, 0, 10, 0);
        NetworkType.removeNetworkTypeRelatedFlags(flags);
        flags.add(portalNetwork.getType().getRelatedFlag());
        return generateFakePortal(topLeft, portalNetwork, name, createInterServerPortal, flags, new HashSet<>(), new StargateRegistry(new StorageMock()));
    }

    /**
     * Generates a random set of portal flags
     *
     * @return <p>A random set of portal flags</p>
     */
    private static Set<PortalFlag> generateRandomFlags() {
        PortalFlag[] possibleFlags = PortalFlag.values();
        Random random = new Random();
        int flagsToGenerate = random.nextInt(possibleFlags.length);
        Set<PortalFlag> flags = EnumSet.noneOf(PortalFlag.class);

        for (int i = 0; i < flagsToGenerate; i++) {
            flags.add(possibleFlags[random.nextInt(possibleFlags.length)]);
        }
        return flags;
    }

    /**
     * Generates a fake portal facing east
     *
     * @param topLeft                 <p>Topleft location of the portal </p>
     * @param network                 <p>The network of the generated portal</p>
     * @param name                    <p>The name of the generated portal</p>
     * @param createInterServerPortal <p>Whether to generate a fake inter-server portal</p>
     * @param flags                   <p>The flags of the portal</p>
     * @return <p>A fake portal</p>
     * @throws InvalidStructureException <p>If an invalid structure is encountered</p>
     * @throws NameLengthException       <p>IF the length of the name is invalid</p>
     */
    public static RealPortal generateFakePortal(Location topLeft, Network network, String name,
                                         boolean createInterServerPortal, Set<PortalFlag> flags, Set<Character> unknownFlags, RegistryAPI registry)
            throws InvalidStructureException, NameLengthException {
        if (createInterServerPortal) {
            flags.add(PortalFlag.FANCY_INTER_SERVER);
        }
        BlockFace facing = BlockFace.EAST;
        String gateFileName = "nether.gate";
        StorageType portalType = createInterServerPortal ? StorageType.INTER_SERVER : StorageType.LOCAL;
        GateData gateData = new GateData(gateFileName, topLeft.getBlockX(), topLeft.getBlockY(), topLeft.getBlockZ(), topLeft.getWorld().getName(),false, topLeft, facing);

        Gate gate = new Gate(gateData, registry);

        gate.addPortalPosition(new BlockVector(1, -2, 0), PositionType.BUTTON, "Stargate");
        gate.addPortalPosition(new BlockVector(1, -2, -3), PositionType.SIGN, "Stargate");
        return new FixedPortal(network, name, "", flags, unknownFlags, gate, UUID.randomUUID(), new LanguageManagerMock(), new StargateEconomyManagerMock());
    }

    public static RealPortal generateFakePortal(Block signBlock, Network network, Set<PortalFlag> flags, String name,
                                                StargateAPI stargateAPI) throws NoFormatFoundException, GateConflictException, TranslatableException {
        Gate gate = PortalCreationHelper.createGate(signBlock, false, stargateAPI.getRegistry());
        flags.add(network.getType().getRelatedFlag());


        RealPortal portal = PortalCreationHelper.createPortal(network, name, "destination", "server", flags, new HashSet<>(), gate, UUID.randomUUID(), stargateAPI);
        network.addPortal(portal, true);
        return portal;
    }

    public static RealPortal generateFakePortal(Block signBlock, String networkName, Set<PortalFlag> flags, String name,
                                                StargateAPI stargateAPI) throws TranslatableException, NoFormatFoundException, GateConflictException {
        Network network = stargateAPI.getRegistry().createNetwork(networkName, NetworkType.CUSTOM, false, false);
        return generateFakePortal(signBlock, network, flags, name, stargateAPI);
    }

}
