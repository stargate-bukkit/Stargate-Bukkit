package org.sgrewritten.stargate.container;

/**
 * Just a small container class for storing two values
 *
 * @param <K> <p>The type of the first tuple value</p>
 * @param <T> <p>The type of the second tuple value</p>
 * @author Kristian
 */
public class TwoTuple<K, T> {

    private final K value1;
    private final T value2;

    /**
     * Instantiates a new two-tuple
     *
     * @param value1 <p>The first value to store</p>
     * @param value2 <p>The second value to store</p>
     */
    public TwoTuple(K value1, T value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Gets the first value stored in this two-tuple
     *
     * @return <p>The first value stored in this two-tuple</p>
     */
    public K getFirstValue() {
        return value1;
    }

    /**
     * Gets the second value of this two-tuple
     *
     * @return <p>The second value of this two-tuple</p>
     */
    public T getSecondValue() {
        return value2;
    }

}
