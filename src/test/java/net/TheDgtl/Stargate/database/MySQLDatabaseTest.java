package net.TheDgtl.Stargate.database;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Order;

public class MySQLDatabaseTest {

    private static DriverEnum driver;
    private static String address;
    private static int port;
    private static String databaseName;
    private static String username;
    private static String password;
    private static boolean useSSL;

    @BeforeClass
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
        Assert.assertNotNull(database);
    }
}
