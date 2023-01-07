package org.sgrewritten.stargate.network.portal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.economy.FakeEconomyManager;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.name.BungeeNameException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;
import org.sgrewritten.stargate.util.portal.PortalCreationHelper;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
     * @param logger
     * @return <p>A map from the portal's name to the portal's object</p>
     * @throws InvalidStructureException <p>If an invalid structure is encountered</p>
     * @throws InvalidNameException        <p>If the generated portal name is invalid</p>
     * @throws NameLengthException 
     */
    public Map<String, RealPortal> generateFakePortals(World world, Network portalNetwork,
                                                       boolean createInterServerPortals, int numberOfPortals,
                                                       StargateLogger logger) throws InvalidStructureException, InvalidNameException, NameLengthException {
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
     * @throws InvalidNameException        <p>If the given portal name is invalid</p>
     * @throws NameLengthException 
     */
    public RealPortal generateFakePortal(World world, Network portalNetwork, String name, boolean createInterServerPortal)
            throws InvalidStructureException, InvalidNameException, NameLengthException {
        Set<PortalFlag> flags = generateRandomFlags();
        //To avoid using the Portal#open method on constructor, which uses an unimplemented function in MockBukkit (block-states)
        flags.remove(PortalFlag.ALWAYS_ON);
        Location topLeft = new Location(world,0,10,0);
        NetworkType.removeNetworkTypeRelatedFlags(flags);
        flags.add(portalNetwork.getType().getRelatedFlag());
        return generateFakePortal(topLeft,portalNetwork,name,createInterServerPortal,flags,new StargateRegistry(new FakeStorage()));
    }

    /**
     * Generates a random set of portal flags
     *
     * @return <p>A random set of portal flags</p>
     */
    private Set<PortalFlag> generateRandomFlags() {
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
     * @param topLeft                       <p>Topleft location of the portal </p>
     * @param network                       <p>The network of the generated portal</p>
     * @param name                          <p>The name of the generated portal</p>
     * @param createInterServerPortal       <p>Whether to generate a fake inter-server portal</p>
     * @param flags                         <p>The flags of the portal</p>
     * @return <p>A fake portal</p>
     * @throws InvalidStructureException    <p>If an invalid structure is encountered</p>
     * @throws NameLengthException          <p>IF the length of the name is invalid</p>
     * @throws InvalidNameException         <p>If the given portal name is invalid</p>
     */
    public RealPortal generateFakePortal(Location topLeft, Network network, String name,
            boolean createInterServerPortal, Set<PortalFlag> flags, RegistryAPI registry )
            throws InvalidStructureException, NameLengthException, InvalidNameException {
        if (createInterServerPortal) {
            flags.add(PortalFlag.FANCY_INTER_SERVER);
        }
        PortalData portalData = new PortalData();
        portalData.topLeft = topLeft;
        portalData.facing = BlockFace.EAST;
        portalData.gateFileName = "nether.gate";
        portalData.portalType = createInterServerPortal ? StorageType.INTER_SERVER : StorageType.LOCAL;

        Gate gate = new Gate(portalData,registry);

        gate.addPortalPosition(new BlockVector(1, -2, 0), PositionType.BUTTON);
        gate.addPortalPosition(new BlockVector(1, -2, -3), PositionType.SIGN);
        return new FixedPortal(network, name, "", flags, gate, UUID.randomUUID(), new FakeLanguageManager(),new FakeEconomyManager());
    }

    public RealPortal generateFakePortal(Block signBlock, Network network, Set<PortalFlag> flags, String name,RegistryAPI registry) throws NameLengthException, BungeeNameException, InvalidNameException, NoFormatFoundException, GateConflictException, NameConflictException {
        Gate gate = PortalCreationHelper.createGate(signBlock, false, registry);
        flags.add(network.getType().getRelatedFlag());
        RealPortal portal =  PortalCreationHelper.createPortal(network, name, "", "", flags, gate, UUID.randomUUID(), new FakeLanguageManager() , registry, new FakeEconomyManager());
        network.addPortal(portal, false);
        return portal;
    }

    public RealPortal generateFakePortal(Block signBlock, String networkName, HashSet<PortalFlag> flags, String name,
            StargateRegistry registry) throws NameLengthException, BungeeNameException, NameConflictException, InvalidNameException, NoFormatFoundException, GateConflictException {
        Network network = registry.createNetwork(networkName, NetworkType.CUSTOM, false, false);
        return generateFakePortal(signBlock, network, flags, name, registry);
    }
}
