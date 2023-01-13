package com.example.testmod.entity.mobs.simple_wizard;

import com.example.testmod.entity.mobs.PatrolNearLocationGoal;
import com.example.testmod.entity.mobs.WizardAttackGoal;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class SimpleWizard extends Animal {
    private static final EntityDataAccessor<Integer> DATA_CASTING_SPELL_ID = SynchedEntityData.defineId(SimpleWizard.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_CASTING_TELEPORT_LOC = SynchedEntityData.defineId(SimpleWizard.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private int castingSpellId = 0;

    public final TeleportSpell teleportSpell = (TeleportSpell) AbstractSpell.getSpell(SpellType.TELEPORT_SPELL, 10);
    public final MagicMissileSpell magicMissileSpell = (MagicMissileSpell) AbstractSpell.getSpell(SpellType.MAGIC_MISSILE_SPELL, 1);

    public SimpleWizard(EntityType<? extends Animal> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CASTING_SPELL_ID, 0);
        this.entityData.define(DATA_CASTING_TELEPORT_LOC, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new PatrolNearLocationGoal(this, 30, .25f));
        this.goalSelector.addGoal(2, new WizardAttackGoal(this, .5, 60));
        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isValidTarget));
    }

    public boolean isValidTarget(@Nullable LivingEntity livingEntity) {
        if (livingEntity != null && livingEntity.isAlive() && livingEntity instanceof Player) {
            return true;
        }
        return false;
    }

    public void setCastingSpell(int spellId, BlockPos blockPos) {

        if (blockPos != null) {
            entityData.set(DATA_CASTING_TELEPORT_LOC, Optional.of(blockPos));
        }

        entityData.set(DATA_CASTING_SPELL_ID, spellId);

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        //TestMod.LOGGER.debug("SimpleWizard.onSyncedDataUpdated: isClientSide: {}", level.isClientSide);

        if (pKey == DATA_CASTING_SPELL_ID) {
            castingSpellId = entityData.get(DATA_CASTING_SPELL_ID);
            //TestMod.LOGGER.debug("SimpleWizard.onSyncedDataUpdated key match: {}", castingSpellId);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level.isClientSide && castingSpellId > 0) {
            if (castingSpellId == teleportSpell.getID()) {
                //TestMod.LOGGER.debug("SimpleWizard.aiStep teleportSpell.onClientPreCast");
                Optional<BlockPos> blockPos = entityData.get(DATA_CASTING_TELEPORT_LOC);
                if (blockPos.isPresent()) {
                    teleportSpell.setTeleportLocation(this, new Vec3(blockPos.get().getX(), blockPos.get().getY(), blockPos.get().getZ()));
                }
                teleportSpell.onClientPreCast(level, this, InteractionHand.MAIN_HAND);
            }
        } else if (!level.isClientSide && castingSpellId > 0 && !entityData.isDirty()) {
            if (castingSpellId == teleportSpell.getID()) {
                //TestMod.LOGGER.debug("SimpleWizard.aiStep setCastingSpell(0)");
                setCastingSpell(0, null);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

//        if (castingSpellId > 0) {
//            TestMod.LOGGER.debug("customServerAiStep {}", level.isClientSide);
//            if (castingSpellId == teleportSpell.getID()) {
//                setCastingSpell(0);
//            }
//        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 1);
    }
}
