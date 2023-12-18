package io.redspace.ironsspellbooks.api.network;

import net.minecraft.network.FriendlyByteBuf;

public interface ISerializable {
    void writeToBuffer(FriendlyByteBuf buffer);

    void readFromBuffer(FriendlyByteBuf buffer);
}
