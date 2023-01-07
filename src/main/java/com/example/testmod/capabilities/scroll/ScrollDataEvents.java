package com.example.testmod.capabilities.scroll;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class ScrollDataEvents {
    public static final String RESOURCE_NAME = "scrollData";
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ScrollData.class);
    }

}