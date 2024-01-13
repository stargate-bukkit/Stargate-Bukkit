package org.sgrewritten.stargate.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MetadataHolder {

    /**
     * Set metadata for this portal. This sets all metadata for this entity, it's advised to use {@link MetadataHolder#setMetadata(JsonElement, String)}
     *
     * @param data <p> The meta data to set </p>
     */
    @ApiStatus.Internal
    void setMetadata(@Nullable String data);

    /**
     * Get metadata for this portal. This gets all metadata for this entity, it's advised to use the {@link MetadataHolder#getMetadata(String)} method
     *
     * @return <p> The meta data of this portal </p>
     */
    @ApiStatus.Internal
    @Nullable
    String getMetadata();

    /**
     * Set the metadata of this instance
     * @param data <p>The data to set</p>
     * @param field <p>The name of the plugin or field this relates to</p>
     */
    default void setMetadata(@Nullable JsonElement data, String field){
        JsonObject metadata = loadMetadata();
        if (data == null) {
            metadata.remove(field);
        } else {
            metadata.add(field, data);
        }
        this.setMetadata(metadata.toString());
    }

    /**
     * Get the metadata of this instance
     * @param field <p>The name of the plugin or field this relates to</p>
     * @return <p>The metadata of this instance</p>
     */
    @Nullable
    default JsonElement getMetadata(String field){
        JsonObject metaData = loadMetadata();
        return metaData.get(field);
    }

    private @NotNull JsonObject loadMetadata(){
        String metadataString = getMetadata();
        JsonObject metadata;
        if (metadataString == null) {
            metadata = new JsonObject();
        } else {
            metadata = JsonParser.parseString(metadataString).getAsJsonObject();
        }
        return metadata;
    }
}
