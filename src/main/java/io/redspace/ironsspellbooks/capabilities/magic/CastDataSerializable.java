package io.redspace.ironsspellbooks.capabilities.magic;

import net.minecraft.network.FriendlyByteBuf;

public interface CastDataSerializable extends CastData {

    void writeToStream(FriendlyByteBuf buffer);

    void readFromStream(FriendlyByteBuf buffer);
}
