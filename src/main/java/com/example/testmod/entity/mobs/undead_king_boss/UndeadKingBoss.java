package com.example.testmod.entity.mobs.undead_king_boss;

import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.goals.PatrolNearLocationGoal;
import com.example.testmod.entity.mobs.goals.WarlockAttackGoal;
import com.example.testmod.registries.AttributeRegistry;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

public class UndeadKingBoss extends AbstractSpellCastingMob implements Enemy {

    public UndeadKingBoss(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 60;
    }

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true).setCreateWorldFog(true);

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WarlockAttackGoal(this, 1f, 40, 80).setSpellLevels(4, 4).setSpells(
                List.of(
                        SpellType.RAY_OF_SIPHONING_SPELL, SpellType.RAY_OF_SIPHONING_SPELL,
                        SpellType.BLOOD_SLASH_SPELL, SpellType.BLOOD_SLASH_SPELL,
                        SpellType.WITHER_SKULL_SPELL, SpellType.WITHER_SKULL_SPELL,
                        SpellType.RAISE_DEAD_SPELL,
                        SpellType.ASCENSION_SPELL,
                        SpellType.FANG_STRIKE_SPELL, SpellType.FANG_STRIKE_SPELL,
                        SpellType.MAGIC_MISSILE_SPELL, SpellType.MAGIC_MISSILE_SPELL
                ),
                List.of(SpellType.FANG_WARD_SPELL, SpellType.FANG_WARD_SPELL, SpellType.EVASION_SPELL),
                List.of(SpellType.BLOOD_STEP_SPELL),
                List.of()
        ));
        this.goalSelector.addGoal(5, new PatrolNearLocationGoal(this, 50, 0.75f));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        //this.goalSelector.addGoal(2,new Mele);

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));

        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        RandomSource randomsource = pLevel.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.TARNISHED_CROWN.get()));
        this.setDropChance(EquipmentSlot.HEAD, 0.05f);
//        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.WANDERING_MAGICIAN_ROBE.get()));
//        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

    }

    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(AttributeRegistry.SPELL_POWER.get(), 1.5)
                .add(Attributes.ARMOR, 8)
                .add(AttributeRegistry.SPELL_RESIST.get(), 1.2)
                .add(Attributes.MAX_HEALTH, 350.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 5.0)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, .20);
    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }
}
