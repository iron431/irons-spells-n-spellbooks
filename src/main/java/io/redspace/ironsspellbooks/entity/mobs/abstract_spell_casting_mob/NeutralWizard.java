package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class NeutralWizard extends AbstractSpellCastingMob implements NeutralMob {
    protected NeutralWizard(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private int lastAngerLevelUpdate;
    /**
     * Tracks spectrum of anger on a per-entity basis, allowing for more nuanced behavior than just "is angry", as well as "anger = hostility"
     * <br>
     * Logically only supports players
     * <br>
     * Serialized to disk
     */
    private final Object2IntArrayMap<UUID> angerLevels = new Object2IntArrayMap<>();

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
        if (!angerLevels.isEmpty()) {
            ListTag levels = new ListTag();
            for (Map.Entry<UUID, Integer> entry : angerLevels.object2IntEntrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("player", entry.getKey());
                tag.putInt("anger", entry.getValue());
                levels.add(tag);
            }
            pCompound.put("angerLevels", levels);
        }
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.readPersistentAngerSaveData(this.level, pCompound);
        if (pCompound.contains("angerLevels")) {
            ListTag entries = pCompound.getList("angerLevels", CompoundTag.TAG_COMPOUND);
            for (Tag tag : entries) {
                try {
                    this.angerLevels.put(((CompoundTag) tag).getUUID("player"), ((CompoundTag) tag).getInt("anger"));
                } catch (Exception exception) {
                    continue;
                }
            }
        }

        super.readAdditionalSaveData(pCompound);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level, true);
        }
        if (!angerLevels.isEmpty() && lastAngerLevelUpdate + 20 * 20 < tickCount) {
            ObjectIterator<Object2IntMap.Entry<UUID>> it = angerLevels.object2IntEntrySet().iterator();
            while (it.hasNext()) {
                Object2IntMap.Entry<UUID> entry = it.next();
                int newLevel = entry.getIntValue() - 1;
                if (newLevel == 0) {
                    it.remove();
                } else {
                    angerLevels.put(entry.getKey(), newLevel);
                }
            }
            lastAngerLevelUpdate = tickCount;
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() instanceof Player player && !player.isCreative()) {
            increaseAngerLevel(player, (int) Math.ceil(pAmount), !isHostileTowards(player));
        }
        return super.hurt(pSource, pAmount);
    }

    public void increaseAngerLevel(Player angryAt, int levels, boolean showParticles) {
        if (level.isClientSide) {
            return;
        }
        int anger = Math.min(angerLevels.getOrDefault(angryAt.getUUID(), 0) + levels, 10);
        angerLevels.put(angryAt.getUUID(), anger);
        lastAngerLevelUpdate = tickCount;
        if (anger < getAngerThreshold() && showParticles) {
            MagicManager.spawnParticles(level, ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 1.25, getZ(), 15, .3, .2, .3, 0, false);
            getAngerSound().ifPresent((sound) -> playSound(sound, getSoundVolume(), getVoicePitch()));
        }
        if (anger >= getAngerThreshold()) {
            setPersistentAngerTarget(angryAt.getUUID());
        }
    }

    @Deprecated
    public void increaseAngerLevel(int levels, boolean showParticles) {
        IronsSpellbooks.LOGGER.warn("Warning! Use of deprecated NeutralWizard#increaseAngerLevel");
        ObjectIterator<Object2IntMap.Entry<UUID>> it = angerLevels.object2IntEntrySet().iterator();
        while (it.hasNext()) {
            Object2IntMap.Entry<UUID> entry = it.next();
            int newLevel = entry.getIntValue() + 1;
            entry.setValue(newLevel);
        }
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
        return isAngryAt(entity) && (entity.getType() != EntityType.PLAYER || angerLevels.getOrDefault(entity.getUUID(), 0) >= getAngerThreshold());
    }

    @Override
    public boolean isAngryAt(LivingEntity pTarget) {
        return (pTarget.getType() == EntityType.PLAYER && angerLevels.containsKey(pTarget.getUUID()))
                || NeutralMob.super.isAngryAt(pTarget);
    }

    /**
     * @return Returns whether this entity cares to guard {@link io.redspace.ironsspellbooks.util.ModTags#GUARDED_BY_WIZARDS} (ie chests)
     */
    public boolean guardsBlocks() {
        return true;
    }

    @Override
    public void readPersistentAngerSaveData(Level level, CompoundTag tag) {
        this.setRemainingPersistentAngerTime(tag.getInt("AngerTime"));
        if (level instanceof ServerLevel) {
            if (!tag.hasUUID("AngryAt")) {
                this.setPersistentAngerTarget(null);
            } else {
                UUID uuid = tag.getUUID("AngryAt");
                this.setPersistentAngerTarget(uuid);
            }
        }
    }

    @Override
    public void playerDied(@NotNull Player player) {
        NeutralMob.super.playerDied(player);
        if (player.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            angerLevels.removeInt(player.getUUID());
        }
    }
}
