package net.TheDgtl.Stargate.refactoring;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import net.TheDgtl.Stargate.FakeStargate;
import net.TheDgtl.Stargate.StargateLogger;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RefactorerTest {
    @Test
    @Order(0)
    public void loadConfigTest() {
        
    }

}
