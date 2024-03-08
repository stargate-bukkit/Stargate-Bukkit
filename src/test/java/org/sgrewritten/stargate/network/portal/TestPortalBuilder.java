package org.sgrewritten.stargate.network.portal;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.sgrewritten.stargate.api.gate.ExplicitGateBuilder;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateBuilder;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.economy.StargateEconomyManagerMock;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.behavior.NetworkedBehavior;
import org.sgrewritten.stargate.network.portal.behavior.PortalBehavior;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.util.LanguageManagerMock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Removes a lot of checks usually done with {@link org.sgrewritten.stargate.api.network.PortalBuilder} and does not
 * save to storage
 */
public class TestPortalBuilder {

    private final RegistryAPI registryAPI;
    private final World world;
    private Network network;
    private Set<PortalFlag> flags;
    private UUID ownerUUID;
    private GateAPI gate;
    private GateBuilder gateBuilder;
    private int gateCounter = 0;
    private PortalBehavior behavior;
    private StorageType storageType = StorageType.LOCAL;
    private String name;
    private Set<Character> unrecognisedFlags;

    public TestPortalBuilder(RegistryAPI registryAPI, World world) {
        this.registryAPI = registryAPI;
        this.world = world;
    }

    public TestPortalBuilder setNetwork(Network network) {
        this.network = Objects.requireNonNull(network);
        return this;
    }

    public TestPortalBuilder setFlags(Set<PortalFlag> flags) {
        this.flags = new HashSet<>(Objects.requireNonNull(flags));
        return this;
    }

    public TestPortalBuilder setUnrecognisedFlags(Set<Character> flags) {
        this.unrecognisedFlags = new HashSet<>(Objects.requireNonNull(flags));
        return this;
    }

    public TestPortalBuilder setOwner(OfflinePlayer owner) {
        this.ownerUUID = owner.getUniqueId();
        return this;
    }

    public TestPortalBuilder setGate(GateAPI gateAPI) {
        this.gate = gateAPI;
        return this;
    }

    public TestPortalBuilder setGateBuilder(GateBuilder gateBuilder) {
        this.gateBuilder = gateBuilder;
        return this;
    }

    public TestPortalBuilder setBehavior(PortalBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    public TestPortalBuilder setStorageType(StorageType storageType) {
        this.storageType = storageType;
        return this;
    }

    public TestPortalBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public RealPortal build() throws InvalidStructureException, GateConflictException, NoFormatFoundException, NameLengthException, NameConflictException {
        if (gate == null) {
            if (gateBuilder == null) {
                gate = findDefaultGateBuilder().build();
            } else {
                gate = gateBuilder.build();
            }
        }
        if (behavior == null) {
            behavior = new NetworkedBehavior(new LanguageManagerMock());
        }
        if (network == null) {
            network = registryAPI.getNetwork(StargateConstant.DEFAULT_NETWORK_ID, storageType);
        }
        if (flags == null) {
            flags = new HashSet<>();
        }
        if (unrecognisedFlags == null) {
            unrecognisedFlags = new HashSet<>();
        }
        if (storageType == StorageType.LOCAL) {
            flags.remove(PortalFlag.INTER_SERVER);
        } else {
            flags.add(PortalFlag.INTER_SERVER);
        }
        flags.add(network.getType().getRelatedFlag());
        if (name == null) {
            name = "portal";
        }
        if (ownerUUID == null) {
            ownerUUID = UUID.randomUUID();
        }
        RealPortal output = new StargatePortal(network, findPortalName(name), new HashSet<>(flags), unrecognisedFlags, gate, ownerUUID,
                new LanguageManagerMock(), new StargateEconomyManagerMock(), null);
        output.setBehavior(behavior);
        return output;
    }

    public Map<String, RealPortal> buildMultiple(int amount) throws InvalidStructureException, GateConflictException, NameLengthException, NameConflictException, NoFormatFoundException {
        Map<String, RealPortal> output = new HashMap<>();
        for (int i = 0; i < amount; i++) {
            RealPortal portal = build();
            network.addPortal(portal);
            output.put(portal.getId(),portal);
        }
        return output;
    }

    private String findPortalName(String initialTry) {
        if (!network.isPortalNameTaken(initialTry)) {
            return initialTry;
        }
        int counter = 0;
        String portalName;
        do {
            portalName = initialTry + counter;
            counter++;
        } while (network.isPortalNameTaken(portalName));
        return portalName;
    }

    public GateBuilder findDefaultGateBuilder() {
        GateBuilder output = new ExplicitGateBuilder(registryAPI, new Location(world, gateCounter * 5, 10, 0), GateFormatRegistry.getFormat("nether.gate"));
        gateCounter++;
        return output;
    }
}
