package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class NeutralWizard extends AbstractSpellCastingMob implements NeutralMob {
    protected NeutralWizard(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private int lastAngerLevelUpdate;
    private int angerLevel;

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public void setRemainingPersistentAngerTime(int pTime) {
        this.remainingPersistentAngerTime = pTime;
    }

    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    public void setPersistentAngerTarget(@javax.annotation.Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }

    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        this.addPersistentAngerSaveData(pCompound);
        pCompound.putInt("AngerLevel", angerLevel);
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.readPersistentAngerSaveData(this.level, pCompound);
        this.angerLevel = pCompound.getInt("AngerLevel");

        super.readAdditionalSaveData(pCompound);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level, true);
        }
        if (angerLevel > 0 && lastAngerLevelUpdate + 20 * 20 < tickCount) {
            angerLevel--;
            lastAngerLevelUpdate = tickCount;
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() instanceof Player player && !player.isCreative()) {
            increaseAngerLevel((int) Math.ceil(pAmount),!isHostileTowards(player));
        }
        return super.hurt(pSource, pAmount);
    }

    public void increaseAngerLevel(int levels, boolean showParticles) {
        if (!level.isClientSide && angerLevel < getAngerThreshold() && showParticles) {
            MagicManager.spawnParticles(level, ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 1.25, getZ(), 15, .3, .2, .3, 0, false);
            getAngerSound().ifPresent((sound) -> playSound(sound, getSoundVolume(), getVoicePitch()));
        }
        angerLevel = Math.min(angerLevel + levels, 10);
        lastAngerLevelUpdate = tickCount;
    }

    public Optional<SoundEvent> getAngerSound() {
        return Optional.empty();
    }

    /**
     * @return The amount of anger triggers (ie chests opened, amount of damage taken) that must be met in order to become hostile
     */
    public int getAngerThreshold() {
        return 2;
    }

    public boolean isHostileTowards(LivingEntity entity) {
        return isAngryAt(entity) && angerLevel >= getAngerThreshold();
    }

    @Override
    public boolean isAngryAt(LivingEntity pTarget) {
        return angerLevel > 0 && NeutralMob.super.isAngryAt(pTarget);
    }

    /**
     * @return Returns whether or not this entity cares to guard {@link io.redspace.ironsspellbooks.util.ModTags#GUARDED_BY_WIZARDS} (ie chests)
     */
    public boolean guardsBlocks() {
        return true;
    }
}
