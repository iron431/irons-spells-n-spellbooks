package com.example.testmod.entity;

import com.example.testmod.TestMod;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.spells.fire.FireballSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
    //TODO: probably need a way to control the spell level dynamically.
    // I'm not going to add this until we have an idea of what we want

    private static final EntityDataAccessor<Integer> DATA_CASTING_SPELL_ID = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CASTING_SPELL_DURATION = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_CASTING_TELEPORT_LOC = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public TeleportSpell teleportSpell;
    public MagicMissileSpell magicMissileSpell;
    public FireballSpell fireballSpell;

    private int castDurationRemaining = -1;
    private boolean castStarted = false;

    private boolean forceLookAtTarget = false;

    protected AbstractSpellCastingMob(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CASTING_SPELL_ID, 0);
        this.entityData.define(DATA_CASTING_SPELL_DURATION, -1);
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
                        prepareSpell(getTarget(), false, SpellType.TELEPORT_SPELL, 10);
                    }
                    case FIREBALL_SPELL -> {
                        prepareSpell(getTarget(), true, SpellType.FIREBALL_SPELL, 1);
                        startCasting(fireballSpell, true);
                    }
                }
            }
        }
    }

    private void resetCastingSpell() {
        entityData.set(DATA_CASTING_TELEPORT_LOC, Optional.empty());
        entityData.set(DATA_CASTING_SPELL_DURATION, -1);
        entityData.set(DATA_CASTING_SPELL_ID, 0);
        castDurationRemaining = -1;
        castStarted = false;
        forceLookAtTarget = false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        var spellId = entityData.get(DATA_CASTING_SPELL_ID);

        if (!level.isClientSide || spellId == 0) {
            return;
        }

        //TestMod.LOGGER.debug("ASCM.aiStep");
        if (castDurationRemaining > 0) {
            castDurationRemaining--;
        }

        TestMod.LOGGER.debug("ASCM.aiStep: castingSpellId:{}, castDurationRemaining:{}, castStarted:{}", spellId, castDurationRemaining, castStarted);

        if (castDurationRemaining <= 0) {
            switch (SpellType.values()[spellId]) {
                case TELEPORT_SPELL -> {
                    entityData.get(DATA_CASTING_TELEPORT_LOC).ifPresent(pos -> {
                        TestMod.LOGGER.debug("ASCM client side teleport actions");
                        teleportSpell.setTeleportLocation(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                        teleportSpell.onClientPreCast(level, this, InteractionHand.MAIN_HAND);
                    });
                }
                case FIREBALL_SPELL -> {
                    //TODO: Stop long Casting animation here?
                }
            }
            resetCastingSpell();
        } else if (!castStarted) {
            castStarted = true;
            switch (SpellType.values()[spellId]) {
                case FIREBALL_SPELL -> {
                    //TODO: Start long Casting animation here?
                }
            }
        } else { //Actively casting a long cast or continuous cast
            switch (SpellType.values()[spellId]) {
                case FIREBALL_SPELL -> {
                    TestMod.LOGGER.debug("ASCM fireball client side particles");
                    addClientSideParticles();
                }
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

        if (castDurationRemaining > 0) {
            castDurationRemaining--;
        }

        //TestMod.LOGGER.debug("ASCM.customServerAiStep: {}", spellId);

        if (castDurationRemaining <= 0) {
            switch (SpellType.values()[spellId]) {
                case TELEPORT_SPELL -> {
                    entityData.get(DATA_CASTING_TELEPORT_LOC).ifPresent(pos -> {
                        this.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
                        teleportSpell.setTeleportLocation(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                        teleportSpell.onCast(this.level, this, null);

                    });
                }
                case FIREBALL_SPELL -> {
                    this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.0f);
                    fireballSpell.onCast(level, this, null);
                }
            }

            resetCastingSpell();
        } else if (!castStarted) {
            castStarted = true;
            switch (SpellType.values()[spellId]) {
                case FIREBALL_SPELL -> {
                    TestMod.LOGGER.debug("ASCM: Fireball start cast");
                    this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
                    //TODO: Start long Casting animation here?
                }
            }
        }
    }

    public void castFireball(boolean forceLookAtTarget, int level) {
        var target = getTarget();
        prepareSpell(target, forceLookAtTarget, SpellType.FIREBALL_SPELL, level);
        startCasting(fireballSpell, true);
    }

    public void castMagicMissile(boolean forceLookAtTarget, int level) {
        prepareSpell(getTarget(), true, SpellType.MAGIC_MISSILE_SPELL, level);
        magicMissileSpell.onCast(this.level, this, null);
    }

    public void castTelportToLoc(Vec3 pos) {
        prepareSpell(getTarget(), false, SpellType.TELEPORT_SPELL, 10);

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

    private void startCasting(AbstractSpell spell, boolean forceLookAtTarget) {
        entityData.set(DATA_CASTING_SPELL_ID, spell.getID());

        if (spell.getCastType() == CastType.LONG) {
            entityData.set(DATA_CASTING_SPELL_DURATION, spell.getCastTime());
            castDurationRemaining = spell.getCastTime();
            this.forceLookAtTarget = forceLookAtTarget;
        }
    }

    private void prepareSpell(LivingEntity target, boolean forceLookAtTarget, SpellType spellType, int level) {
        switch (spellType) {
            case TELEPORT_SPELL -> {
                if (teleportSpell == null)
                    teleportSpell = (TeleportSpell) AbstractSpell.getSpell(SpellType.TELEPORT_SPELL, level);
            }
            case MAGIC_MISSILE_SPELL -> {
                if (magicMissileSpell == null)
                    magicMissileSpell = (MagicMissileSpell) AbstractSpell.getSpell(SpellType.MAGIC_MISSILE_SPELL, level);
                else
                    magicMissileSpell.setLevel(level);
            }
            case FIREBALL_SPELL -> {
                if (fireballSpell == null)
                    fireballSpell = (FireballSpell) AbstractSpell.getSpell(SpellType.FIREBALL_SPELL, level);
                else
                    fireballSpell.setLevel(level);
            }
        }

        if (forceLookAtTarget && target != null) {
            lookAt(target, 180, 180);
        }
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
