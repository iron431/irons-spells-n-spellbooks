package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.NBT;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class DeadKingSoulEntity extends Entity implements AntiMagicSusceptible {
    private static final EntityDataAccessor<Vector3f> DATA_RESPAWN_POS = SynchedEntityData.defineId(DeadKingSoulEntity.class, EntityDataSerializers.VECTOR3);
    private static final double MOVE_PER_TICK = .12;

    public DeadKingSoulEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public DeadKingSoulEntity(Level level, Vec3 start, Vec3 respawnPos) {
        this(EntityRegistry.DEAD_KING_SOUL.get(), level);
        this.setPos(start);
        this.setRespawnPos(respawnPos);
    }

    public void setRespawnPos(Vec3 respawnPos) {
        this.entityData.set(DATA_RESPAWN_POS, respawnPos.toVector3f());
    }

    public Vec3 getRespawnPos() {
        Vector3f respawnPos = this.entityData.get(DATA_RESPAWN_POS);
        return new Vec3(respawnPos);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RESPAWN_POS, Vec3.ZERO.toVector3f());
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 vec, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (isAtSpawn() && itemStack.is(ModTags.DEAD_KING_RESPAWNABLE)) {
            if (player.level instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    Vec3 particlePos = player.getEyePosition().add(player.getForward()).subtract(0, 0.3, 0);
                    MagicManager.spawnParticles(serverLevel, new ItemParticleOption(ParticleTypes.ITEM, itemStack), particlePos.x, particlePos.y, particlePos.z, 9, .15, .15, .15, 0.08, false);
                    itemStack.shrink(1);
                    player.setItemInHand(hand, itemStack);
                }
                this.playSound(SoundEvents.DECORATED_POT_SHATTER, 2, 0.75f);
                this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2, 0.75f);
                MagicManager.spawnParticles(level, ParticleTypes.SCULK_SOUL, getX(), getY(), getZ(), 50, .1, .1, .1, 0.3, false);

                DeadKingCorpseEntity deadKingCorpseEntity = new DeadKingCorpseEntity(EntityRegistry.DEAD_KING_CORPSE.get(), serverLevel);
                deadKingCorpseEntity.moveTo(this.position());
                float f = player.getYRot() + 180;
                // set the y rot dammit
                deadKingCorpseEntity.setYRot(f);
                deadKingCorpseEntity.yRotO = f;
                deadKingCorpseEntity.yHeadRot = f;
                deadKingCorpseEntity.yHeadRotO = f;
                deadKingCorpseEntity.yBodyRot = f;
                deadKingCorpseEntity.yBodyRotO = f;
                deadKingCorpseEntity.trigger();

                serverLevel.addFreshEntity(deadKingCorpseEntity);
                this.discard();
            }
            return InteractionResult.SUCCESS;
        }
        return super.interactAt(player, vec, hand);
    }

    @Override
    public void tick() {
        super.tick();
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        this.xo = d0;
        this.yo = d1;
        this.zo = d2;
        if (level().isClientSide) {
            double halfWidth = getBbWidth() * 0.5;
            int count = isAtSpawn() ? 1 : 3;
            for (int i = 0; i < count; i++) {
                Vec3 offset = Utils.getRandomVec3(halfWidth);
                Vec3 speed = Utils.getRandomVec3(0.05);
                speed = speed.add(0, 0.05, 0);
                level().addParticle(
                        ParticleHelper.SOUL_FIRE,
                        true,
                        getX() + offset.x,
                        getY() + halfWidth + offset.y,
                        getZ() + offset.z,
                        speed.x,
                        speed.y,
                        speed.z
                );
            }
        }
        if (!isAtSpawn()) {
            Vec3 pos = position();
            Vec3 to = getRespawnPos().subtract(pos);
            double len = to.length();
            Vec3 step = to.normalize().scale(Math.min(MOVE_PER_TICK, len));
            move(MoverType.SELF, step);
        }
    }

    public boolean isAtSpawn() {
        return getRespawnPos().equals(Vec3.ZERO) || getRespawnPos().subtract(position()).lengthSqr() < 0.1 * 0.1;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("RespawnPos", Tag.TAG_COMPOUND)) {
            this.setRespawnPos(NBT.readVec3(compound.getCompound("RespawnPos")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if(!getRespawnPos().equals(Vec3.ZERO)) {
            compound.put("RespawnPos", NBT.writeVec3Pos(getRespawnPos()));
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.playSound(SoundRegistry.APPLY_EFFECT_BAD_OMEN.get(), 2f, 1f);
        MagicManager.spawnParticles(level, ParticleHelper.SOUL_FIRE, getX(), getY(), getZ(), 50, .1, .1, .1, 0.3, false);
        this.discard();
    }
}
