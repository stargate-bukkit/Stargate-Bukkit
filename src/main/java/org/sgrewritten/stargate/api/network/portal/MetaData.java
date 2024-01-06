package org.sgrewritten.stargate.api.network.portal;

import org.jetbrains.annotations.Nullable;

/**
 * Convenience class to hold one string value that can be changed without losing the reference.
 */
public class MetaData {
    private String metaDataString;

    public MetaData(String metaDataString) {
        this.metaDataString = metaDataString;
    }

    /**
     * @return <p>The stored metadata string</p>
     */
    public @Nullable String getMetaDataString() {
        return metaDataString;
    }

    /**
     * @param metaDataString <p>The metadata string to be stored</p>
     */
    public void setMetaDataString(@Nullable String metaDataString) {
        this.metaDataString = metaDataString;
    }
}
