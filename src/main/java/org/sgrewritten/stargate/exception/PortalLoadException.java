package org.sgrewritten.stargate.exception;

public class PortalLoadException extends Exception{

    private final FailureType failureType;

    public PortalLoadException(FailureType failureType){
        super(failureType.name());
        this.failureType = failureType;
    }

    public FailureType getFailureType(){
        return this.failureType;
    }

    public enum FailureType{
        GATE_FORMAT,
        WORLD;
    }
}
