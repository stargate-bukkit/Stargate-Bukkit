package org.sgrewritten.stargate.network.portal;

import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.PortalType;

import java.util.EnumSet;
import java.util.HashMap;
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
     * @throws NameErrorException        <p>If the generated portal name is invalid</p>
     */
    public Map<String, RealPortal> generateFakePortals(World world, Network portalNetwork,
                                                       boolean createInterServerPortals, int numberOfPortals,
                                                       StargateLogger logger) throws InvalidStructureException, NameErrorException {
        Map<String, RealPortal> output = new HashMap<>();
        String baseName;
        if (createInterServerPortals) {
            baseName = interPortalDefaultName;
        } else {
            baseName = portalDefaultName;
        }

        for (int portalNumber = 0; portalNumber < numberOfPortals; portalNumber++) {
            String name = baseName + portalNumber;
            RealPortal portal = generateFakePortal(world, portalNetwork, name, createInterServerPortals, logger);
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
     * @throws NameErrorException        <p>If the given portal name is invalid</p>
     */
    public RealPortal generateFakePortal(World world, Network portalNetwork, String name, boolean createInterServerPortal,
                                         StargateLogger logger) throws InvalidStructureException, NameErrorException {
        Set<PortalFlag> flags = generateRandomFlags();
        if (createInterServerPortal) {
            flags.add(PortalFlag.FANCY_INTER_SERVER);
        }
        PortalData portalData = new PortalData();
        portalData.topLeft = world.getBlockAt(0, 0, 0).getLocation();
        portalData.facing = BlockFace.EAST;
        portalData.gateFileName = "fileName.gate";
        portalData.portalType = createInterServerPortal ? PortalType.INTER_SERVER : PortalType.LOCAL;

        Gate gate = new Gate(portalData, logger);

        gate.addPortalPosition(new BlockVector(1, -2, 0), PositionType.BUTTON);
        gate.addPortalPosition(new BlockVector(1, -2, -3), PositionType.SIGN);
        //To avoid using the Portal#open method on constructor, which uses an unimplemented function in MockBukkit (block-states)
        flags.remove(PortalFlag.ALWAYS_ON);
        return new FixedPortal(portalNetwork, name, "", flags, gate, UUID.randomUUID(), logger);
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

}
