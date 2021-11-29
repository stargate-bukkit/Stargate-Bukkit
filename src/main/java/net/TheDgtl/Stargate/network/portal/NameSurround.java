package net.TheDgtl.Stargate.network.portal;

public enum NameSurround {
    PORTAL("-", "-"), DESTINATION(">", "<"), NETWORK("(", ")"), BUNGEE("[", "]"), NOTHING("", ""), PERSONAL("{", "}");

    private final String begin;
    private final String end;

    NameSurround(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }

    public String getSurround(String aName) {
        return begin + aName + end;
    }
}
