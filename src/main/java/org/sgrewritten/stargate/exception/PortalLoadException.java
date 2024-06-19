package org.sgrewritten.stargate.exception;

public class PortalLoadException extends Exception{

    private final FailureType failureType;

    /**
     * @param failureType <p>How the portal could not load</p>
     */
    public PortalLoadException(FailureType failureType){
        super(failureType.name());
        this.failureType = failureType;
    }

    /**
     * @return <p>How the portal could not load</p>
     */
    public FailureType getFailureType(){
        return this.failureType;
    }

    public enum FailureType{
        /**
         * Unable to load the portal, as the gate format did not exist
         */
        GATE_FORMAT,

        /**
         * Unable to load the portal as the world did not exist
         */
        WORLD;
    }
}
