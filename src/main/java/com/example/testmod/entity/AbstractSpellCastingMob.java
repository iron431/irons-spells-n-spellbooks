package com.example.testmod.entity;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.spells.fire.FireballSpell;
import com.example.testmod.spells.ice.ConeOfColdSpell;
import com.example.testmod.spells.lightning.ElectrocuteSpell;
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

    private TeleportSpell teleportSpell;
    private MagicMissileSpell magicMissileSpell;
    private FireballSpell fireballSpell;
    private ConeOfColdSpell coneOfColdSpell;
    private ElectrocuteSpell electrocuteSpell;

    private PlayerMagicData playerMagicData = new PlayerMagicData();
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
                    case CONE_OF_COLD_SPELL -> {
                        TestMod.LOGGER.debug("ASCM.castConeOfCold");
                        prepareSpell(getTarget(), true, SpellType.CONE_OF_COLD_SPELL, 1);
                        startCasting(coneOfColdSpell, true);
                    }
                    case ELECTROCUTE_SPELL -> {
                        TestMod.LOGGER.debug("ASCM.ELECTROCUTE_SPELL");
                        prepareSpell(getTarget(), true, SpellType.ELECTROCUTE_SPELL, 1);
                        startCasting(electrocuteSpell, true);
                    }
                }
            }
        }
    }

    private void castComplete(AbstractSpell spell) {
        if (!level.isClientSide) {
            spell.onCastComplete(level, this, playerMagicData);
        }

        playerMagicData.resetCastingState();

        entityData.set(DATA_CASTING_TELEPORT_LOC, Optional.empty());
        entityData.set(DATA_CASTING_SPELL_DURATION, -1);
        entityData.set(DATA_CASTING_SPELL_ID, 0);
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

        //TestMod.LOGGER.debug("ASCM.aiStep: castingSpellId:{}, castDurationRemaining:{}, castStarted:{}", spellId, castDurationRemaining, castStarted);

        if (playerMagicData.getCastDurationRemaining() <= 0) {
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

        playerMagicData.handleCastDuration();

        //TestMod.LOGGER.debug("ASCM.customServerAiStep: {}", spellId);

        if (playerMagicData.getCastDurationRemaining() <= 0) {
            if (forceLookAtTarget) {
                forceLookAtTarget(getTarget());
            }

            switch (SpellType.values()[spellId]) {
                case TELEPORT_SPELL -> {
                    entityData.get(DATA_CASTING_TELEPORT_LOC).ifPresent(pos -> {
                        this.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
                        teleportSpell.setTeleportLocation(this, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                        teleportSpell.onCast(this.level, this, null);
                        castComplete(teleportSpell);
                    });
                }
                case FIREBALL_SPELL -> {
                    this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.0f);
                    fireballSpell.onCast(level, this, null);
                    castComplete(fireballSpell);
                }
                case CONE_OF_COLD_SPELL -> {
                    TestMod.LOGGER.debug("ASCM.customServerAiStep  castComplete(coneOfColdSpell)");
                    castComplete(coneOfColdSpell);
                }
                case ELECTROCUTE_SPELL -> {
                    TestMod.LOGGER.debug("ASCM.customServerAiStep  castComplete(coneOfColdSpell)");
                    castComplete(electrocuteSpell);
                }
            }
        } else if (!castStarted) {
            castStarted = true;
            switch (SpellType.values()[spellId]) {
                case FIREBALL_SPELL -> {
                    TestMod.LOGGER.debug("ASCM: Fireball start cast");
                    this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
                    //TODO: Start long Casting animation here?
                }
                case CONE_OF_COLD_SPELL -> {
                    TestMod.LOGGER.debug("ASCM: coneOfColdSpell start cast");
                    coneOfColdSpell.onCast(level, this, playerMagicData);
                }
                case ELECTROCUTE_SPELL -> {
                    TestMod.LOGGER.debug("ASCM: coneOfColdSpell start cast");
                    electrocuteSpell.onCast(level, this, playerMagicData);
                }
            }
        } else {
            switch (SpellType.values()[spellId]) {
                case CONE_OF_COLD_SPELL -> {
                    TestMod.LOGGER.debug("ASCM: coneOfColdSpell tick cast");
                    if ((playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
                        if (forceLookAtTarget && getTarget() != null) {
                            forceLookAtTarget(getTarget());
                        }
                        coneOfColdSpell.onCast(level, this, playerMagicData);
                    }
                }
                case ELECTROCUTE_SPELL -> {
                    TestMod.LOGGER.debug("ASCM: coneOfColdSpell tick cast");
                    if ((playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
                        if (forceLookAtTarget && getTarget() != null) {
                            forceLookAtTarget(getTarget());
                        }
                        electrocuteSpell.onCast(level, this, playerMagicData);
                    }
                }
            }
        }
    }

    public void castFireball(boolean forceLookAtTarget, int spellLevel) {
        var target = getTarget();
        prepareSpell(target, forceLookAtTarget, SpellType.FIREBALL_SPELL, spellLevel);
        startCasting(fireballSpell, true);
    }

    public void castMagicMissile(boolean forceLookAtTarget, int spellLevel) {
        prepareSpell(getTarget(), true, SpellType.MAGIC_MISSILE_SPELL, spellLevel);
        magicMissileSpell.onCast(this.level, this, null);
    }

    public void castTelportToLoc(Vec3 pos) {
        prepareSpell(getTarget(), false, SpellType.TELEPORT_SPELL, 10);

        entityData.set(DATA_CASTING_TELEPORT_LOC, Optional.of(new BlockPos(pos)));
        entityData.set(DATA_CASTING_SPELL_ID, teleportSpell.getID());
    }

    public void castConeOfCold(boolean forceLookAtTarget, int spellLevel) {
        TestMod.LOGGER.debug("ASCM.castConeOfCold forceLook: {}, spellLevel: {}", forceLookAtTarget, spellLevel);
        prepareSpell(getTarget(), forceLookAtTarget, SpellType.CONE_OF_COLD_SPELL, spellLevel);
        startCasting(coneOfColdSpell, true);
    }

    public void castElectrocute(boolean forceLookAtTarget, int spellLevel) {
        TestMod.LOGGER.debug("ASCM.castConeOfCold forceLook: {}, spellLevel: {}", forceLookAtTarget, spellLevel);
        prepareSpell(getTarget(), forceLookAtTarget, SpellType.ELECTROCUTE_SPELL, spellLevel);
        startCasting(electrocuteSpell, true);
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

    public boolean isCasting() {
        return castStarted;
    }

    private void startCasting(AbstractSpell spell, boolean forceLookAtTarget) {
        entityData.set(DATA_CASTING_SPELL_ID, spell.getID());

        playerMagicData.initiateCast(spell.getID(), spell.getLevel(), spell.getCastTime());

        if (spell.getCastType() == CastType.LONG || spell.getCastType() == CastType.CONTINUOUS) {
            entityData.set(DATA_CASTING_SPELL_DURATION, spell.getCastTime());
            this.forceLookAtTarget = forceLookAtTarget;
        }
    }

    private void prepareSpell(LivingEntity target, boolean forceLookAtTarget, SpellType spellType, int spellLevel) {
        this.forceLookAtTarget = forceLookAtTarget;

        switch (spellType) {
            case TELEPORT_SPELL -> {
                if (teleportSpell == null)
                    teleportSpell = (TeleportSpell) AbstractSpell.getSpell(SpellType.TELEPORT_SPELL, spellLevel);
            }
            case MAGIC_MISSILE_SPELL -> {
                if (magicMissileSpell == null)
                    magicMissileSpell = (MagicMissileSpell) AbstractSpell.getSpell(SpellType.MAGIC_MISSILE_SPELL, spellLevel);
                else
                    magicMissileSpell.setLevel(spellLevel);
            }
            case FIREBALL_SPELL -> {
                if (fireballSpell == null)
                    fireballSpell = (FireballSpell) AbstractSpell.getSpell(SpellType.FIREBALL_SPELL, spellLevel);
                else
                    fireballSpell.setLevel(spellLevel);
            }
            case CONE_OF_COLD_SPELL -> {
                if (coneOfColdSpell == null)
                    coneOfColdSpell = (ConeOfColdSpell) AbstractSpell.getSpell(SpellType.CONE_OF_COLD_SPELL, spellLevel);
                else
                    coneOfColdSpell.setLevel(spellLevel);
            }
            case ELECTROCUTE_SPELL -> {
                if (electrocuteSpell == null)
                    electrocuteSpell = (ElectrocuteSpell) AbstractSpell.getSpell(SpellType.ELECTROCUTE_SPELL, spellLevel);
                else
                    electrocuteSpell.setLevel(spellLevel);
            }
        }

        if (this.forceLookAtTarget && target != null) {
            forceLookAtTarget(target);
        }
    }

    private void forceLookAtTarget(LivingEntity target) {
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
