package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.network.FriendlyByteBuf;

public class ImpulseCastData implements ICastDataSerializable {
    public float x;
    public float y;
    public float z;
    public boolean hasImpulse;

    public ImpulseCastData() {
    }

    public ImpulseCastData(float x, float y, float z, boolean hasImpulse) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hasImpulse = hasImpulse;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeBoolean(hasImpulse);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.x = buffer.readFloat();
        this.y = buffer.readFloat();
        this.z = buffer.readFloat();
        this.hasImpulse = buffer.readBoolean();
    }

    @Override
    public void reset() {

    }
}
