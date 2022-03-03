package net.TheDgtl.Stargate.property;

/**
 * An enum that contains versions that implemented various Spigot features
 */
public enum VersionImplemented {

    /**
     * The version that added chat color
     */
    CHAT_COLOR(1, 16);

    private final int releaseVersion;
    private final int majorVersion;

    /**
     * Instantiates a new version implemented
     *
     * @param releaseVersion <p>The release version part of the version</p>
     * @param majorVersion   <p>The major version part of the version</p>
     */
    VersionImplemented(int releaseVersion, int majorVersion) {
        this.releaseVersion = releaseVersion;
        this.majorVersion = majorVersion;
    }

    /**
     * Checks whether the version this feature was implemented is older than the given version
     *
     * <p>If the version this feature was added is older than the given version, that means the given version is newer,
     * and is thus able to support this feature.</p>
     *
     * @param releaseVersion <p>The release version part of the version to check</p>
     * @param majorVersion   <p>The major version part of the version to check</p>
     * @return <p>True if this feature version is older than the given version</p>
     */
    public boolean isOlderThan(int releaseVersion, int majorVersion) {
        return releaseVersion >= this.releaseVersion && majorVersion >= this.majorVersion;
    }

}
