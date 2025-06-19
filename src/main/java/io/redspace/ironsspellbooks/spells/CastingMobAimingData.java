package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

public class CastingMobAimingData implements ICastDataSerializable {
    private Vec3 aimPosition = Vec3.ZERO;
    private Vec3 lastAimPosition = Vec3.ZERO;

    public void updateAim(Entity target, float strength) {
        Vec3 wanted = target.getBoundingBox().getCenter();
        if (aimPosition.equals(Vec3.ZERO)) {
            aimPosition = wanted;
            lastAimPosition = wanted;
        } else {
            lastAimPosition = aimPosition;
            aimPosition = aimPosition.add(wanted.subtract(aimPosition).scale(strength));
        }
    }

    public Vec3 getAimPosition() {
        return aimPosition;
    }

    public Vec3 getAimPosition(float partialTick) {
        return lastAimPosition.add(aimPosition.subtract(lastAimPosition).scale(partialTick));
    }

    public Vec3 getForward(Entity host) {
        return aimPosition.subtract(host.getEyePosition()).normalize();
    }

    @Override
    public void reset() {

    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt((int) (aimPosition.x * 10));
        buffer.writeInt((int) (aimPosition.y * 10));
        buffer.writeInt((int) (aimPosition.z * 10));
        buffer.writeInt((int) (lastAimPosition.x * 10));
        buffer.writeInt((int) (lastAimPosition.y * 10));
        buffer.writeInt((int) (lastAimPosition.z * 10));
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        aimPosition = new Vec3(buffer.readInt() / 10.0, buffer.readInt() / 10.0, buffer.readInt() / 10.0);
        lastAimPosition = new Vec3(buffer.readInt() / 10.0, buffer.readInt() / 10.0, buffer.readInt() / 10.0);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

    }
}
