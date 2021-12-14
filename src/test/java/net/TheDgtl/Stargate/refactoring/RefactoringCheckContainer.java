package net.TheDgtl.Stargate.refactoring;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for containing several types of refactoring checks
 */
public class RefactoringCheckContainer {

    private final Map<String, Object> settingChecks;
    private final Map<String, String> portalChecks;

    /**
     * Instantiates a new refactoring checker
     *
     * @param settingChecks <p>The setting checks to test for the relevant refactorer</p>
     * @param portalChecks  <p>The portals checks to test for the relevant refactorer</p>
     */
    public RefactoringCheckContainer(Map<String, Object> settingChecks, Map<String, String> portalChecks) {
        this.settingChecks = settingChecks;
        this.portalChecks = portalChecks;
    }

    /**
     * Gets the setting checks to test for the relevant refactorer
     *
     * <p>A map of settings and their values</p>
     *
     * @return <p>The settings to test for the relevant refactorer</p>
     */
    public Map<String, Object> getSettingChecks() {
        return new HashMap<>(this.settingChecks);
    }

    /**
     * Gets the portal checks to test for the relevant refactorer
     *
     * <p>Key is a portal name, value is its network name</p>
     *
     * @return <p>The portal checks to test for the relevant refactorer</p>
     */
    public Map<String, String> getPortalChecks() {
        return new HashMap<>(this.portalChecks);
    }

}
