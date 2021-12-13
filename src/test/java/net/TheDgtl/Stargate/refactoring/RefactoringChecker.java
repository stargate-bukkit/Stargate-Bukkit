package net.TheDgtl.Stargate.refactoring;

import java.util.HashMap;

public class RefactoringChecker {
    /**
     * A map of settings and their values
     */
    public HashMap<String,Object> settingCheckers = new HashMap<>();
    /**
     * Key is a portal name, value is its network name
     */
    public HashMap<String,String> portalChecker = new HashMap<>();
}
