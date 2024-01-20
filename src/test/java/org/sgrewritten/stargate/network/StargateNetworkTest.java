package org.sgrewritten.stargate.network;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.proxy.InterServerMessageSender;
import org.sgrewritten.stargate.network.proxy.LocalNetworkMessageSender;
import org.sgrewritten.stargate.util.StargateTestHelper;

import java.util.UUID;

public class StargateNetworkTest {

    private static final String NET_NAME = "network";

    @BeforeEach
    void setup() {
        StargateTestHelper.setup();
    }

    @AfterEach
    void teardown() {
        StargateTestHelper.tearDown();
    }

    @ParameterizedTest
    @EnumSource
    void getMessageSender_interServer(NetworkType type) throws InvalidNameException, UnimplementedFlagException, NameLengthException {
        if (type == NetworkType.TERMINAL) {
            return;
        }
        Network network = new StargateNetwork(nameFromNetworkType(type), type, StorageType.INTER_SERVER);
        Assertions.assertInstanceOf(InterServerMessageSender.class, network.getPluginMessageSender());
    }

    @ParameterizedTest
    @EnumSource
    void getMessageSender_localServer(NetworkType type) throws InvalidNameException, UnimplementedFlagException, NameLengthException {
        if (type == NetworkType.TERMINAL) {
            return;
        }
        Network network = new StargateNetwork(nameFromNetworkType(type), type, StorageType.LOCAL);
        Assertions.assertInstanceOf(LocalNetworkMessageSender.class, network.getPluginMessageSender());
    }

    private String nameFromNetworkType(NetworkType type) {
        return switch (type) {
            case CUSTOM -> NET_NAME;
            case PERSONAL -> UUID.randomUUID().toString();
            case DEFAULT -> StargateNetwork.DEFAULT_NETWORK_ID;
            default -> null;
        };
    }
}
