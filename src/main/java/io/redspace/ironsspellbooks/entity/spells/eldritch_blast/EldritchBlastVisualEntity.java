package io.redspace.ironsspellbooks.entity.spells.eldritch_blast;

import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
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

public class EldritchBlastVisualEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final int lifetime = 8;

    public EldritchBlastVisualEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public float distance;

    public EldritchBlastVisualEntity(Level level, Vec3 start, Vec3 end, LivingEntity owner) {
        super(EntityRegistry.ELDRITCH_BLAST_VISUAL_ENTITY.get(), level);
        this.setPos(start);
        this.distance = (float) start.distanceTo(end);
        this.setRot(owner.getYRot(), owner.getXRot());
    }

    @Override
    public void tick() {
        if (tickCount == 1) {
            if (level.isClientSide) {
                var forward = getForward();
                for (float i = 1; i < distance; i += .5f) {
                    Vec3 pos = position().add(forward.scale(i));
                    level.addParticle(ParticleTypes.SMOKE, false, pos.x, pos.y + .5, pos.z, 0, 0, 0);
                }
            }
        } else if (tickCount > lifetime) {
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
