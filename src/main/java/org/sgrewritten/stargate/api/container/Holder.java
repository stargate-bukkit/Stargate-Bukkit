package org.sgrewritten.stargate.api.container;

public class Holder<T>{

    /**
     * The value in this holder
     */
    public T value;

    /**
     * @param value <p>A value of this holder</p>
     */
    public Holder(T value){
        this.value = value;
    }
}
