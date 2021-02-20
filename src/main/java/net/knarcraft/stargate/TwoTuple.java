package net.knarcraft.stargate;

/**
 * This class allows storing two values of any type
 *
 * @param <K> <p>The first type</p>
 * @param <L> <p>The second type</p>
 */
public class TwoTuple<K, L> {

    private K firstValue;
    private L secondValue;

    /**
     * Instantiate a new TwoTuple
     *
     * @param firstValue  <p>The first value</p>
     * @param secondValue <p>The second value</p>
     */
    public TwoTuple(K firstValue, L secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    /**
     * Gets the first value
     *
     * @return <p>The first value</p>
     */
    public K getFirstValue() {
        return firstValue;
    }

    /**
     * Gets the second value
     *
     * @return <p>The second value</p>
     */
    public L getSecondValue() {
        return secondValue;
    }

}
