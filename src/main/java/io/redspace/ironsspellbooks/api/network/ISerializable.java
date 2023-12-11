package io.redspace.ironsspellbooks.api.network;

import net.minecraft.network.FriendlyByteBuf;

public interface ISerializable {
    void writeToStream(FriendlyByteBuf buffer);

    void readFromStream(FriendlyByteBuf buffer);
}
