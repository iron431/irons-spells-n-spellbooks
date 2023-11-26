package io.redspace.ironsspellbooks.entity.spells.ray_of_frost;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class RayOfFrostVisualEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final int lifetime = 15;
    public RayOfFrostVisualEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public float distance;

    public RayOfFrostVisualEntity(Level level, Vec3 start, Vec3 end, LivingEntity owner) {
        super(EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get(), level);
        this.setPos(start.subtract(0, .75f, 0));
        this.distance = (float) start.distanceTo(end);
        this.setRot(owner.getYRot(), owner.getXRot());
    }

    @Override
    public void tick() {
        if (++tickCount > lifetime) {
            this.discard();
        }
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt((int) (distance * 10));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.distance = additionalData.readInt() / 10f;
    }
}
