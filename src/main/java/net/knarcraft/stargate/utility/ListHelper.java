package net.knarcraft.stargate.utility;

import java.util.List;
import java.util.Random;

/**
 * A helper class for dealing with lists
 */
public final class ListHelper {

    private static final Random random = new Random();

    private ListHelper() {

    }

    /**
     * Gets a random item from a list
     *
     * @param list <p>The list to get an item from</p>
     * @param <T>  <p>The type of item the list contains</p>
     * @return <p>A random item</p>
     */
    public static <T> T getRandom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

}
