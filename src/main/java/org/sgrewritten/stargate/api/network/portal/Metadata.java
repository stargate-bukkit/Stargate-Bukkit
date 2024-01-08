package org.sgrewritten.stargate.api.network.portal;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.MetadataHolder;

/**
 * Convenience class to hold one string value that can be changed without losing the reference.
 */
public class Metadata implements MetadataHolder {
    private String metaDataString;

    public Metadata(String metaDataString) {
        this.metaDataString = metaDataString;
    }

    @Override
    public void setMetadata(@Nullable String data) {
        this.metaDataString = data;
    }

    @Override
    public @Nullable String getMetadata() {
        return this.metaDataString;
    }
}
