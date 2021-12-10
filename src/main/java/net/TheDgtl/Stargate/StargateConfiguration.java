package net.TheDgtl.Stargate;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class StargateConfiguration extends YamlConfiguration {

    private String contents;

    @Override
    public String saveToString() {
        return null;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        this.contents = contents;
        super.loadFromString(contents);
    }

    @Override
    protected String buildHeader() {
        return "";
    }

}
