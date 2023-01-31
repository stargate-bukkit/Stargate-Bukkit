package org.sgrewritten.stargate.network.portal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.StorageType;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.economy.FakeEconomyManager;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.gate.control.ButtonControlMechanism;
import org.sgrewritten.stargate.gate.control.SignControlMechanism;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;

import java.util.*;

/**
 * A generator for generating fake portals
 */
public class FakePortalGenerator {

    private final String portalDefaultName;
    private final String interPortalDefaultName;

    /**
     * Instantiates a new fake portal generator. When generating multiple portals,
     * Local portals will have a name based on "portal", and InterServer portals,
     * "iportal".
     */
    public FakePortalGenerator() {
        this("portal", "iportal");
    }

    /**
     * Instantiates a new fake portal generator
     *
     * @param portalBaseName      <p>The base-name used for all fake portals</p>
     * @param interPortalBaseName <p>The base-name used for all fake inter-portals</p>
     */
    public FakePortalGenerator(String portalBaseName, String interPortalBaseName) {
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
    public RealPortal generateFakePortal(World world, Network portalNetwork, String name, boolean createInterServerPortal)
            throws InvalidStructureException, TranslatableException {
        Set<PortalFlag> flags = generateRandomFlags();
        //To avoid using the Portal#open method on constructor, which uses an unimplemented function in MockBukkit (block-states)
        flags.remove(PortalFlag.ALWAYS_ON);
        Location topLeft = new Location(world, 0, 10, 0);
        NetworkType.removeNetworkTypeRelatedFlags(flags);
        flags.add(portalNetwork.getType().getRelatedFlag());
        return generateFakePortal(topLeft, portalNetwork, name, createInterServerPortal, flags, new StargateRegistry(new FakeStorage()));
    }

    /**
     * Generates a random set of portal flags
     *
     * @return <p>A random set of portal flags</p>
     */
    private Set<PortalFlag> generateRandomFlags() {
        List<PortalFlag> possibleFlags = PortalFlag.values();
        Random random = new Random();
        int flagsToGenerate = random.nextInt(possibleFlags.size());
        Set<PortalFlag> flags = new HashSet<>();

        for (int i = 0; i < flagsToGenerate; i++) {
            flags.add(possibleFlags.get(random.nextInt(possibleFlags.size())));
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
    public RealPortal generateFakePortal(Location topLeft, Network network, String name,
                                         boolean createInterServerPortal, Set<PortalFlag> flags, RegistryAPI registry)
            throws InvalidStructureException, NameLengthException {
        if (createInterServerPortal) {
            flags.add(PortalFlag.FANCY_INTER_SERVER);
        }
        PortalData portalData = new PortalData();
        portalData.topLeft = topLeft;
        portalData.facing = BlockFace.EAST;
        portalData.gateFileName = "nether.gate";
        portalData.portalType = createInterServerPortal ? StorageType.INTER_SERVER : StorageType.LOCAL;
        portalData.flags = flags;

        Gate gate = new Gate(portalData, registry, new FakeLanguageManager());

        List<GatePosition> portalPositions = List.of(new ButtonControlMechanism(new BlockVector(1, -2, 0), gate), new SignControlMechanism(new BlockVector(1, -2, -3), gate, new FakeLanguageManager()));
        gate.addPortalPositions(portalPositions);
        return new FixedPortal(network, name, "", flags, gate, UUID.randomUUID(), new FakeLanguageManager(), new FakeEconomyManager());
    }

    public static RealPortal generateFakePortal(Block signBlock, Network network, Set<PortalFlag> flags, String name, String destination, String server, RegistryAPI registry) throws GateConflictException, NoFormatFoundException, TranslatableException {
        Gate gate = PortalCreationHelper.createGate(signBlock, flags.contains(PortalFlag.ALWAYS_ON), registry, new FakeLanguageManager());
        flags.add(network.getType().getRelatedFlag());

        RealPortal portal = PortalCreationHelper.createPortal(network, name, destination, server, flags, gate, UUID.randomUUID(), new FakeLanguageManager(), registry, new FakeEconomyManager());
        network.addPortal(portal, true);
        return portal;
    }

    public static RealPortal generateFakePortal(Block signBlock, Network network, Set<PortalFlag> flags, String name,
                                                RegistryAPI registry) throws NoFormatFoundException, GateConflictException, TranslatableException {
        return generateFakePortal(signBlock, network, flags, name, "destination", "server", registry);
    }

    public static RealPortal generateFakePortal(Block signBlock, String networkName, Set<PortalFlag> flags, String name,
                                                StargateRegistry registry) throws TranslatableException, NoFormatFoundException, GateConflictException {
        Network network = registry.createNetwork(networkName, NetworkType.CUSTOM, false, false);
        return generateFakePortal(signBlock, network, flags, name, registry);
    }

}
