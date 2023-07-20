package org.sgrewritten.stargate.container;

public class ThreeTuple<K, T, G> {



    private final K value1;
    private final T value2;
    private final G value3;

    /**
     * Instantiates a new three-tuple
     *
     * @param value1 <p>The first value to store</p>
     * @param value2 <p>The second value to store</p>
     */
    public ThreeTuple(K value1, T value2, G value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    /**
     * Gets the first value stored in this three-tuple
     *
     * @return <p>The first value stored in this two-tuple</p>
     */
    public K getFirstValue() {
        return value1;
    }

    /**
     * Gets the second value of this three-tuple
     *
     * @return <p>The second value of this two-tuple</p>
     */
    public T getSecondValue() {
        return value2;
    }

    /**
     * Gets the third value of this three-tuple
     *
     * @return <p>The third value of this three-tuple</p>
     */
    public G getThirdValue(){
        return value3;
    }

    @Override
    public String toString() {
        return String.format("ThreeTuple(%s,%s,%s)",value1.toString(),value2.toString(),value3.toString());
    }
}
