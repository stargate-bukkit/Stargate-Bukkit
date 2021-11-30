package net.TheDgtl.Stargate.database;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;

public class MySQLDatabaseTest {

    public static DriverEnum driver = DriverEnum.MARIADB;
    public static String address;
    public static int port;
    public static String databaseName;
    public static String username;
    public static String password;
    public static boolean useSSL;

    @BeforeAll
    public static void setUp() {
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
