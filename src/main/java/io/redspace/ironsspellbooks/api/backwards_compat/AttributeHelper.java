package io.redspace.ironsspellbooks.api.backwards_compat;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class AttributeHelper {

    public static UUID uuidFromId(ResourceLocation location) {
        return UUID.nameUUIDFromBytes(location.toString().getBytes());
    }
}
