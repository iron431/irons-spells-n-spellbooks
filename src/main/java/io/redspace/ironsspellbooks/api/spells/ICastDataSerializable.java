package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.network.FriendlyByteBuf;

public interface ICastDataSerializable extends ICastData {

    void writeToStream(FriendlyByteBuf buffer);

    void readFromStream(FriendlyByteBuf buffer);
}
