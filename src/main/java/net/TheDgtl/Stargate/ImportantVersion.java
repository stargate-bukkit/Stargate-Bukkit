package net.TheDgtl.Stargate;

/**
 * An enum for storing important versions
 */
public enum ImportantVersion {

    /**
     * The last version before chat color was implemented
     */
    NO_CHAT_COLOR_IMPLEMENTED(1, 15);

    private final int releaseVersion;
    private final int majorVersion;

    /**
     * Instantiates a new important version
     *
     * @param releaseVersion <p>The release version part of the version</p>
     * @param majorVersion   <p>The major version part of the version</p>
     */
    ImportantVersion(int releaseVersion, int majorVersion) {
        this.releaseVersion = releaseVersion;
        this.majorVersion = majorVersion;
    }

    /**
     * Checks whether the given version is newer than this important version
     *
     * @param releaseVersion <p>The release version part of the version to check</p>
     * @param majorVersion   <p>The major version part of the version to check</p>
     * @return <p>True if the given version is newer than this important version</p>
     */
    public boolean isOlderThan(int releaseVersion, int majorVersion) {
        return releaseVersion >= this.releaseVersion && majorVersion > this.majorVersion;
    }

}
