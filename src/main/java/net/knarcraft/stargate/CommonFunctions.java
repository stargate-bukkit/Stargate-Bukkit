package net.knarcraft.stargate;

import java.io.InputStream;

/*
 * stargate - A portal plugin for Bukkit
 * Copyright (C) 2021 Kristian Knarvik
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A holding class for methods shared between classes
 *
 * @author Kristian Knarvik <kristian.knarvik@knett.no>
 */
public final class CommonFunctions {

    private CommonFunctions() {}

    /**
     * Gets a resource as an InputStream
     *
     * @param resourceName <p>The name of the resource you want to readFromServer</p>
     * @return <p>An input stream which can be used to access the resource</p>
     */
    public static InputStream getResourceAsStream(String resourceName) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return classloader.getResourceAsStream(resourceName);
    }

}
