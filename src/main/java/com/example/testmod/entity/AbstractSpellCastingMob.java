package com.example.testmod.entity;

import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractSpellCastingMob extends PathfinderMob {

    private static final EntityDataAccessor<Integer> DATA_CASTING_SPELL_ID = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_CASTING_TELEPORT_LOC = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public TeleportSpell teleportSpell;
    public MagicMissileSpell magicMissileSpell;

    protected AbstractSpellCastingMob(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CASTING_SPELL_ID, 0);
        this.entityData.define(DATA_CASTING_TELEPORT_LOC, Optional.empty());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);

        if (level.isClientSide) {
            if (pKey == DATA_CASTING_SPELL_ID) {
                var castingSpellId = entityData.get(DATA_CASTING_SPELL_ID);
                switch (SpellType.values()[castingSpellId]) {
                    case TELEPORT_SPELL -> {
                        teleportSpell = (TeleportSpell) AbstractSpell.getSpell(SpellType.TELEPORT_SPELL, 10);
                    }
                }
            }
        }
    }

    public void resetCastingSpell() {
        entityData.set(DATA_CASTING_TELEPORT_LOC, Optional.empty());
        entityData.set(DATA_CASTING_SPELL_ID, 0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        var spellId = entityData.get(DATA_CASTING_SPELL_ID);

        if (!level.isClientSide || spellId == 0) {
            return;
        }

        //TestMod.LOGGER.debug("ASCM.aiStep");

        switch (SpellType.values()[spellId]) {
            case TELEPORT_SPELL -> {
                entityData.get(DATA_CASTING_TELEPORT_LOC).ifPresent(pos -> {
                    teleportSpell.setTeleportLocation(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                    teleportSpell.onClientPreCast(level, this, InteractionHand.MAIN_HAND);
                });
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        var spellId = entityData.get(DATA_CASTING_SPELL_ID);

        if (spellId == 0 || entityData.isDirty()) {
            return;
        }

        //TestMod.LOGGER.debug("ASCM.customServerAiStep: {}", spellId);

        switch (SpellType.values()[spellId]) {
            case TELEPORT_SPELL -> {
                entityData.get(DATA_CASTING_TELEPORT_LOC).ifPresent(pos -> {
                    teleportSpell.setTeleportLocation(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                    teleportSpell.onCast(this.level, this, null);
                    resetCastingSpell();
                });
            }
        }
    }

    public void castMagicMissile(boolean forceLookAtTarget, int level) {
        if (magicMissileSpell == null) {
            magicMissileSpell = (MagicMissileSpell) AbstractSpell.getSpell(SpellType.MAGIC_MISSILE_SPELL, level);
        }

        if (level != magicMissileSpell.getLevel()) {
            magicMissileSpell.setLevel(level);
        }

        var target = getTarget();

        if (forceLookAtTarget && target != null) {
            this.lookAt(target, 180, 180);
        }
        this.magicMissileSpell.onCast(this.level, this, null);
    }

    public void castTelportToLoc(Vec3 pos) {
        if (teleportSpell == null) {
            teleportSpell = (TeleportSpell) AbstractSpell.getSpell(SpellType.TELEPORT_SPELL, 10);
        }

        entityData.set(DATA_CASTING_TELEPORT_LOC, Optional.of(new BlockPos(pos)));
        entityData.set(DATA_CASTING_SPELL_ID, teleportSpell.getID());
    }

    public void castTelportBehindTarget(LivingEntity target, int distance) {
        var rotation = target.getLookAngle().normalize().scale(-distance);
        var pos = target.position();
        var dest = rotation.add(pos);
        castTelportToLoc(dest);
    }

    public boolean isValidTarget(@Nullable LivingEntity livingEntity) {
        if (livingEntity != null && livingEntity.isAlive() && livingEntity instanceof Player) {
            return true;
        }
        return false;
    }
}
