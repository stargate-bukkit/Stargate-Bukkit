/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.TheDgtl.Stargate.database;

/**
 * Enum to list drivers.
 *
 * @author Frostalf
 */
public enum DriverEnum {
    MYSQL,
    MARIADB,
    SQLITE,
    POSTREGSQL;

    public static DriverEnum parse(String setting) {
        return valueOf(setting.toUpperCase());
    }
}
