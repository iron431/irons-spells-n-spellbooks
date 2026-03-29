package io.redspace.ironsspellbooks.entity.spells.ender_chain;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class EnderChain extends Entity implements AntiMagicSusceptible, IEntityWithComplexSpawn {
    private static final int SEGMENT_COUNT = 8;
    private static final float SEGMENT_SIZE = 0.5f;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    @Nullable
    private UUID victimUUID;
    @Nullable
    private Entity cachedVictim;

    private float health = 10f;
    private int lifetime = 200;
    private float restraintStrength = 0.35f;

    private final EnderChainPart[] parts;

    public EnderChain(EntityType<?> type, Level level) {
        super(type, level);
        this.parts = new EnderChainPart[SEGMENT_COUNT];
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            this.parts[i] = new EnderChainPart(this, SEGMENT_SIZE, SEGMENT_SIZE);
        }
        this.setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public EnderChain(Level level, Entity owner, Entity victim, Vec3 anchor) {
        this(EntityRegistry.ENDER_CHAIN.get(), level);
        setOwner(owner);
        setVictim(victim);
        setPos(anchor);
    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    @Override
    public boolean isAlliedTo(@NotNull Entity entity) {
        return super.isAlliedTo(entity) || entity.getUUID().equals(ownerUUID);
    }

    public void setVictim(@Nullable Entity victim) {
        if (victim != null) {
            this.victimUUID = victim.getUUID();
            this.cachedVictim = victim;
        }
    }

    @Nullable
    public Entity getVictim() {
        if (this.cachedVictim != null && !this.cachedVictim.isRemoved()) {
            return this.cachedVictim;
        } else if (this.victimUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(this.victimUUID);
            if (e instanceof LivingEntity living) {
                this.cachedVictim = living;
                return living;
            }
        }
        return null;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public void setRestraintStrength(float strength) {
        this.restraintStrength = strength;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?> @NotNull [] getParts() {
        return parts;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i].setId(id + i + 1);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            Entity victim = getVictim();
            if (victim == null || victim.isRemoved() || tickCount > lifetime || health <= 0) {
                breakChain();
                return;
            }
            applyRestraint(victim);
        } else {
            chainParticlesAround(this.position(), 3);
            if (parts != null && parts.length > 0) {
                chainParticlesAround(parts[parts.length - 1].position(), 1);
            }
        }

        repositionParts();
    }

    private void chainParticlesAround(Vec3 pos, int count) {
        for (int i = 0; i < count; i++) {
            Vec3 random = Utils.getRandomVec3(0.05);
            level.addParticle(ParticleHelper.UNSTABLE_ENDER, pos.x + random.x, pos.y + random.y, pos.z + random.z, random.x, random.y, random.z);
        }
    }

    private void applyRestraint(Entity victim) {
        Vec3 displacement = victim.position().subtract(position());
        if (displacement.lengthSqr() < 1.0E-8) {
            return;
        }
        Vec3 deltaV = displacement.scale(-restraintStrength);
        victim.setDeltaMovement(victim.getDeltaMovement().add(deltaV));
        victim.hurtMarked = true;
    }

    private void repositionParts() {
        Entity victim = getVictim();
        Vec3 start = position();
        Vec3 end = victim != null ? victim.position().add(0, victim.getBbHeight() * 0.5, 0) : start.add(0, 1, 0);
        int totalLinks = parts.length + 1;

        for (int i = 0; i < parts.length; i++) {
            float t = (float) (i + 1) / totalLinks;
            Vec3 pos = start.lerp(end, t);
            parts[i].xo = pos.x;
            parts[i].yo = pos.y;
            parts[i].zo = pos.z;
            parts[i].xOld = pos.x;
            parts[i].yOld = pos.y;
            parts[i].zOld = pos.z;
            parts[i].setPos(pos);
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (level().isClientSide || isInvulnerableTo(source)) {
            return false;
        }
        if (DamageSources.isFriendlyFireBetween(source.getEntity(), getOwner())) {
            return false;
        }
        health -= amount;
        if (health <= 0) {
            breakChain();
        }
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    public void breakChain() {
        if (!level().isClientSide) {
            this.discard();
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        breakChain();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        if (victimUUID != null) {
            tag.putUUID("Victim", victimUUID);
        }
        tag.putFloat("Health", health);
        tag.putInt("Lifetime", lifetime);
        tag.putInt("Age", tickCount);
        tag.putFloat("RestraintStrength", restraintStrength);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
            this.cachedOwner = null;
        }
        if (tag.hasUUID("Victim")) {
            this.victimUUID = tag.getUUID("Victim");
            this.cachedVictim = null;
        }
        this.health = tag.getFloat("Health");
        this.lifetime = tag.getInt("Lifetime");
        this.tickCount = tag.getInt("Age");
        if (tag.contains("RestraintStrength")) {
            this.restraintStrength = tag.getFloat("RestraintStrength");
        }
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        var owner = getOwner();
        buffer.writeInt(owner == null ? 0 : owner.getId());
        var victim = getVictim();
        buffer.writeInt(victim == null ? 0 : victim.getId());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        Entity owner = this.level.getEntity(additionalData.readInt());
        if (owner != null) {
            this.setOwner(owner);
        }
        Entity victim = this.level.getEntity(additionalData.readInt());
        if (victim != null) {
            this.cachedVictim = victim;
            this.victimUUID = victim.getUUID();
        }
    }
}
