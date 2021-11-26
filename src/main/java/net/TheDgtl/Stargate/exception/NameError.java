package net.TheDgtl.Stargate.exception;

import net.TheDgtl.Stargate.LangMsg;

public class NameError extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -9187508162071170232L;
    private LangMsg msg;

    public NameError(LangMsg msg) {
        this.msg = msg;
    }

    public LangMsg getMsg() {
        return msg;
    }
}