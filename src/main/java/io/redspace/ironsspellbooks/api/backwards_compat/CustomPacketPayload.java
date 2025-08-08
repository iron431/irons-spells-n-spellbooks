package io.redspace.ironsspellbooks.api.backwards_compat;

import net.minecraft.resources.ResourceLocation;

/**
 * Abstract layer for potential forward porting
 */
public interface CustomPacketPayload extends PacketHelper {
    public static record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }
}
