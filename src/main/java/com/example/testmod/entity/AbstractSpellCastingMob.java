package com.example.testmod.entity;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.TeleportSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.EnumMap;

import static com.example.testmod.capabilities.magic.SyncedSpellData.SYNCED_SPELL_DATA;

public abstract class AbstractSpellCastingMob extends Monster {
    private static final EntityDataAccessor<SyncedSpellData> DATA_SPELL = SynchedEntityData.defineId(AbstractSpellCastingMob.class, SYNCED_SPELL_DATA);

    private final EnumMap<SpellType, AbstractSpell> spells = new EnumMap<>(SpellType.class);
    private final PlayerMagicData playerMagicData = new PlayerMagicData();

    private @Nullable AbstractSpell castingSpell;

    protected AbstractSpellCastingMob(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        playerMagicData.setSyncedData(new SyncedSpellData(this));
    }

    public PlayerMagicData getPlayerMagicData() {
        return playerMagicData;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL, new SyncedSpellData(-1));
        //TestMod.LOGGER.debug("ASCM.defineSynchedData DATA_SPELL:{}", DATA_SPELL);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        TestMod.LOGGER.debug("ASCM.onSyncedDataUpdated ENTER level.isClientSide:{} {}", level.isClientSide, pKey);
        super.onSyncedDataUpdated(pKey);

        if (!level.isClientSide) {
            return;
        }

        if (pKey.getId() == DATA_SPELL.getId()) {
            var isCasting = playerMagicData.isCasting();
            var syncedSpellData = entityData.get(DATA_SPELL);
            //TestMod.LOGGER.debug("ASCM.onSyncedDataUpdated(DATA_SPELL) {} {}", level.isClientSide, syncedSpellData);
            playerMagicData.setSyncedData(syncedSpellData);

            if (!syncedSpellData.isCasting() && isCasting) {
                castComplete();
                return;
            } else {
                var spellType = SpellType.getTypeFromValue(syncedSpellData.getCastingSpellId());
                castSpell(spellType, syncedSpellData.getCastingSpellLevel());
            }
        }
    }

    public void doSyncSpellData() {
        //TestMod.LOGGER.debug("ASCM.doSyncSpellData {} {}", level.isClientSide, playerMagicData.getSyncedData());
        //Need a deep clone of the object because set does a basic object ref compare to trigger the update. Do not remove this
        entityData.set(DATA_SPELL, playerMagicData.getSyncedData().deepClone());
    }

    private void castComplete() {
        //TestMod.LOGGER.debug("ASCM.castComplete isClientSide:{}", level.isClientSide);
        if (!level.isClientSide) {
            castingSpell.onServerCastComplete(level, this, playerMagicData);
        }

        playerMagicData.resetCastingState();
        castingSpell = null;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level.isClientSide || castingSpell == null) {
            return;
        }

        if (playerMagicData.getCastDurationRemaining() <= 0) {
            if (castingSpell.getCastType() == CastType.INSTANT) {
                castingSpell.onClientPreCast(level, this, InteractionHand.MAIN_HAND, playerMagicData);
                castComplete();
            }
        } else { //Actively casting a long cast or continuous cast
            if (castingSpell.getSpellType() == SpellType.FIREBALL_SPELL) {
                //TODO: this needs to be handled by abstract spell in some way with an onClientCastTick event
                addClientSideParticles();
            }
        }

        //TODO:  playerMagicData.handleCastDuration(); <-- This may need to be called here?
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (castingSpell == null || entityData.isDirty()) {
            return;
        }

        playerMagicData.handleCastDuration();

        if (playerMagicData.isCasting()) {
            castingSpell.onServerCastTick(level, this, playerMagicData);
        }

        if (playerMagicData.getCastDurationRemaining() <= 0) {
            if (castingSpell.getCastType() == CastType.LONG || castingSpell.getCastType() == CastType.CHARGE || castingSpell.getCastType() == CastType.INSTANT) {
                forceLookAtTarget(getTarget());
                //TestMod.LOGGER.debug("ASCM.customServerAiStep: onCast.1 {}", castingSpell.getSpellType());
                castingSpell.onCast(level, this, playerMagicData);
            }
            castComplete();
        } else if (castingSpell.getCastType() == CastType.CONTINUOUS) {
            if ((playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
                forceLookAtTarget(getTarget());
                //TestMod.LOGGER.debug("ASCM.customServerAiStep: onCast.2 {}", castingSpell.getSpellType());
                castingSpell.onCast(level, this, playerMagicData);
            }
        }
    }

    public void castSpell(SpellType spellType, int spellLevel) {
        //TestMod.LOGGER.debug("ASCM.castSpell: {} {}", spellType, spellLevel);
        if (spellType == SpellType.NONE_SPELL) {
            castingSpell = null;
            return;
        }

        castingSpell = spells.computeIfAbsent(spellType, key -> AbstractSpell.getSpell(spellType, spellLevel));
        this.startUsingItem(InteractionHand.MAIN_HAND);
        playerMagicData.initiateCast(castingSpell.getID(), castingSpell.getLevel(), castingSpell.getCastTime(), CastSource.MOB);

        //TODO: this may be in the wrong spot.. i don't think this works for all cast types here
        if (!level.isClientSide) {
            castingSpell.onServerPreCast(level, this, playerMagicData);
        }
    }

    public boolean isCasting() {
        return playerMagicData != null && playerMagicData.isCasting();
    }

    public void setTeleportLocationBehindTarget(int distance) {
        var target = getTarget();
        if (target != null) {
            var rotation = target.getLookAngle().normalize().scale(-distance);
            var pos = target.position();
            var teleportPos = rotation.add(pos);

            int y = target.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) teleportPos.x, (int) teleportPos.z);

            if (Math.abs(teleportPos.y - y) > 3) {
                rotation = target.getLookAngle().normalize().scale(-((float) distance / 2));
                teleportPos = rotation.add(pos);
                y = target.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) teleportPos.x, (int) teleportPos.z);

                if (Math.abs(teleportPos.y - y) > 3) {
                    rotation = target.getLookAngle().normalize().scale(-1);
                    teleportPos = rotation.add(pos);
                }
            }

            playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
        }
    }

    private void forceLookAtTarget(LivingEntity target) {
        if (target != null)
            lookAt(target, 180, 180);
    }

    private void addClientSideParticles() {
        double d0 = .4d;
        double d1 = .3d;
        double d2 = .35d;
        float f = this.yBodyRot * ((float) Math.PI / 180F) + Mth.cos((float) this.tickCount * 0.6662F) * 0.25F;
        float f1 = Mth.cos(f);
        float f2 = Mth.sin(f);
        this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + (double) f1 * 0.6D, this.getY() + 1.8D, this.getZ() + (double) f2 * 0.6D, d0, d1, d2);
        this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() - (double) f1 * 0.6D, this.getY() + 1.8D, this.getZ() - (double) f2 * 0.6D, d0, d1, d2);
    }
}
