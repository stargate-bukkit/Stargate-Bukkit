package net.TheDgtl.Stargate.refactoring.retcons;

import java.util.HashMap;
import java.util.Map;

public abstract class Modificator {
    /**
     * 
     * @param oldSetting a string where index 0 is the key, and index 1 is the value
     * @return new setting where index 0 is the key, and index 1 is the value
     */
    protected Object[] getNewSetting(Object[] oldSetting) {return oldSetting;}
    
    /**
     * 
     * @param oldConfig
     * @return a new configuration
     */
    public Map<String,Object> run(Map<String,Object> oldConfig) {
        
        return recursiveConfigScroller(oldConfig);
    }
    
    public abstract int getConfigNumber();
    
    private Map<String,Object> recursiveConfigScroller(Map<String,Object> config) {
        Map<String,Object> replacementConfig = new HashMap<>();
        for (String key : config.keySet()) {
            Object[] oldSetting = new Object[] { key, config.get(key) };
            
            Object[] newSetting = getNewSetting(oldSetting);
            if(newSetting == null) {
                newSetting = oldSetting;
            }
            replacementConfig.put((String)newSetting[0], newSetting[1]);
        }
        return replacementConfig;
    }
}