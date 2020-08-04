package tech.wundero.placeholderredux.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

// TODO comments
// TODO additional settings

@ConfigSerializable
public class Config {

    @Setting(comment = "Whether or not to enable verbose logging.")
    public boolean verbose = false;
}
