package io.redspace.ironsspellbooks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfigs {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    static{
        BUILDER.push("Client Configs");

        //Put client stuff here, such as hud element configurations

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
