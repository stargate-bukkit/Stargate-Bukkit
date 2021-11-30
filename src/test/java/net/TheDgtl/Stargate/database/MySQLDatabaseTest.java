package net.TheDgtl.Stargate.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLDatabaseTest {

    private static DriverEnum driver;
    private static String address;
    private static int port;
    private static String databaseName;
    private static String username;
    private static String password;
    private static boolean useSSL;

    @BeforeAll
    public static void setUp() {
        driver = DriverEnum.MARIADB;
        address = "LOCALHOST";
        port = 3306;
        databaseName = "stargate";
        username = "root";
        password = "";
        useSSL = true;
    }

    @Test
    @Order(1)
    public void loadDatabase() {
        Database database = new MySqlDatabase(driver, address, port, databaseName, username, password, useSSL);
        Assertions.assertNotNull(database);
    }
    
}
