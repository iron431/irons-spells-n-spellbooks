package io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist;

import com.google.common.collect.Sets;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.entity.mobs.goals.AlchemistAttackGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardRecoverGoal;
import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;
import io.redspace.ironsspellbooks.item.FurledMapItem;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import io.redspace.ironsspellbooks.player.AdditionalWanderingTrades;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class ApothecaristEntity extends NeutralWizard implements IMerchantWizard {

    public ApothecaristEntity(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 25;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AlchemistAttackGoal(this, 1.25f, 30, 70, 12, 0.5f)
                .setSpells(
                        List.of(SpellRegistry.FANG_STRIKE_SPELL.get(), SpellRegistry.FANG_STRIKE_SPELL.get(), SpellRegistry.ACID_ORB_SPELL.get(), SpellRegistry.POISON_BREATH_SPELL.get(), SpellRegistry.STOMP_SPELL.get(), SpellRegistry.POISON_ARROW_SPELL.get()),
                        List.of(SpellRegistry.ROOT_SPELL.get()),
                        List.of(),
                        List.of(SpellRegistry.OAKSKIN_SPELL.get(), SpellRegistry.STOMP_SPELL.get())
                )
                .setDrinksPotions()
                .setSingleUseSpell(SpellRegistry.FIREFLY_SWARM_SPELL.get(), 80, 200, 4, 6)
                .setSpellQuality(.25f, .60f)
        );
        this.goalSelector.addGoal(3, new PatrolNearLocationGoal(this, 30, .75f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new WizardRecoverGoal(this));

        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractPiglin.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isHostileTowards));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));

    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && swingTime > 0) {
            swingTime--;
        }
    }

    @Override
    public void swing(InteractionHand pHand) {
        swingTime = 10;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        RandomSource randomsource = Utils.random;
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        //this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.PLAGUED_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.PLAGUED_CHESTPLATE.get()));
        //this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.is(DamageTypes.MAGIC) && pSource.getEntity() == this) {
            //prevent our own harm potions from affecting us
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance pEffectInstance) {
        return !AlchemistAttackGoal.ATTACK_POTIONS.contains(pEffectInstance.getEffect());
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, .25);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % 60 == 0) {
            this.level.getEntitiesOfClass(AbstractPiglin.class, this.getBoundingBox().inflate(this.getAttributeValue(Attributes.FOLLOW_RANGE))).forEach((piggy) -> {
                if (PiglinAi.getAngerTarget(piggy).isEmpty() && TargetingConditions.forCombat().test(piggy, this)) {
                    PiglinAi.setAngerTarget(piggy, this);
                }
            });
        }
    }

    /**
     * Merchant implementations
     */

    @Nullable
    private Player tradingPlayer;
    @Nullable
    protected MerchantOffers offers;

    //Serialized
    private long lastRestockGameTime;
    private int numberOfRestocksToday;
    //Not Serialized
    private long lastRestockCheckDayTime;

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        boolean preventTrade = this.getOffers().isEmpty() || this.getTarget() != null || isAngryAt(pPlayer);
        if (pHand == InteractionHand.MAIN_HAND) {
            if (preventTrade && !this.level.isClientSide) {
                //this.setUnhappy();
            }
        }
        if (!preventTrade) {
            if (!this.level.isClientSide && !this.getOffers().isEmpty()) {
                if (shouldRestock()) {
                    restock();
                }
                this.startTrading(pPlayer);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(pPlayer, pHand);
    }

    private void startTrading(Player pPlayer) {
        this.setTradingPlayer(pPlayer);
        this.lookControl.setLookAt(pPlayer);
        this.openTradingScreen(pPlayer, this.getDisplayName(), 0);
    }

    @Override
    public int getRestocksToday() {
        return numberOfRestocksToday;
    }

    @Override
    public void setRestocksToday(int restocks) {
        this.numberOfRestocksToday = restocks;
    }

    @Override
    public long getLastRestockGameTime() {
        return lastRestockGameTime;
    }

    @Override
    public void setLastRestockGameTime(long time) {
        this.lastRestockGameTime = time;
    }

    @Override
    public long getLastRestockCheckDayTime() {
        return lastRestockCheckDayTime;
    }

    @Override
    public void setLastRestockCheckDayTime(long time) {
        this.lastRestockCheckDayTime = time;
    }

    @Override
    public Level merchantLevel() {
        return this.level;
    }

    @Override
    public void setTradingPlayer(@org.jetbrains.annotations.Nullable Player pTradingPlayer) {
        this.tradingPlayer = pTradingPlayer;
    }

    @Override
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();

            this.offers.addAll(createRandomOffers(3, 4));

            if (this.random.nextFloat() < 0.25f) {
                this.offers.add(new AdditionalWanderingTrades.InkBuyTrade((InkItem) ItemRegistry.INK_UNCOMMON.get()).getOffer(this, this.random));
            }
            if (this.random.nextFloat() < 0.25f) {
                this.offers.add(new AdditionalWanderingTrades.InkBuyTrade((InkItem) ItemRegistry.INK_RARE.get()).getOffer(this, this.random));
            }
            if (this.random.nextFloat() < 0.25f) {
                this.offers.add(new AdditionalWanderingTrades.InkBuyTrade((InkItem) ItemRegistry.INK_EPIC.get()).getOffer(this, this.random));
            }
            if (this.random.nextFloat() < 0.5f) {
                this.offers.add(new AdditionalWanderingTrades.ExilirBuyTrade(true, false).getOffer(this, this.random));
            }
            int j = random.nextIntBetweenInclusive(1, 3);
            for (int i = 0; i < j; i++) {
                this.offers.add(random.nextBoolean() ? new AdditionalWanderingTrades.PotionSellTrade(null).getOffer(this, this.random) : new AdditionalWanderingTrades.ExilirSellTrade(true, false).getOffer(this, this.random));
            }
            this.offers.add(new AdditionalWanderingTrades.RandomScrollTrade(new SpellFilter(SchoolRegistry.NATURE.get()), 0f, .4f).getOffer(this, this.random));
            if (this.random.nextFloat() < .65f) {
                this.offers.add(new AdditionalWanderingTrades.RandomScrollTrade(new SpellFilter(SchoolRegistry.NATURE.get()), .5f, .9f).getOffer(this, this.random));
            }

            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 16),
                    ItemStack.EMPTY,
                    new ItemStack(ItemRegistry.NETHERWARD_TINCTURE.get(), 1),
                    0,
                    8,
                    5,
                    0.01f
            ));
            this.offers.removeIf(Objects::isNull);

            //We count the creation of our stock as a restock so that we do not immediately refresh trades the same day.
            numberOfRestocksToday++;
        }
        return this.offers;
    }

    private static final List<MerchantOffer> fillerOffers = List.of(new MerchantOffer(
            new ItemStack(Items.EMERALD, 4),
            ItemStack.EMPTY,
            new ItemStack(Items.MAGMA_CREAM, 1),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 6),
            ItemStack.EMPTY,
            new ItemStack(Items.HONEY_BOTTLE, 2),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 10),
            ItemStack.EMPTY,
            new ItemStack(Items.NETHER_WART, 5),
            0,
            5,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 3),
            ItemStack.EMPTY,
            new ItemStack(Items.GLOWSTONE_DUST),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 3),
            ItemStack.EMPTY,
            new ItemStack(Items.REDSTONE),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 2),
            ItemStack.EMPTY,
            new ItemStack(Items.GLOW_INK_SAC),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 4),
            ItemStack.EMPTY,
            new ItemStack(Items.HONEYCOMB),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 7),
            ItemStack.EMPTY,
            new ItemStack(Items.FERMENTED_SPIDER_EYE, 2),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 12),
            ItemStack.EMPTY,
            new ItemStack(Items.RABBIT_FOOT, 1),
            0,
            3,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 9),
            ItemStack.EMPTY,
            new ItemStack(Items.GLISTERING_MELON_SLICE, 2),
            0,
            4,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 12),
            ItemStack.EMPTY,
            new ItemStack(Items.CRIMSON_FUNGUS, 4),
            0,
            4,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.EMERALD, 12),
            ItemStack.EMPTY,
            new ItemStack(Items.WARPED_FUNGUS, 4),
            0,
            4,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.APPLE, 12),
            ItemStack.EMPTY,
            new ItemStack(Items.EMERALD, 6),
            0,
            6,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.BEETROOT, 10),
            ItemStack.EMPTY,
            new ItemStack(Items.EMERALD, 8),
            0,
            6,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.CARROT, 6),
            ItemStack.EMPTY,
            new ItemStack(Items.EMERALD, 4),
            0,
            6,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.PORKCHOP, 6),
            ItemStack.EMPTY,
            new ItemStack(Items.EMERALD, 6),
            0,
            6,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.DRAGON_BREATH, 1),
            ItemStack.EMPTY,
            new ItemStack(ItemRegistry.ARCANE_ESSENCE.get(), 8),
            0,
            8,
            5,
            0.01f
    ), new MerchantOffer(
            new ItemStack(Items.AXOLOTL_BUCKET, 1),
            ItemStack.EMPTY,
            new ItemStack(Items.EMERALD, 16),
            0,
            1,
            5,
            0.01f
    ));

    private Collection<MerchantOffer> createRandomOffers(int min, int max) {
        Set<Integer> set = Sets.newHashSet();
        int fillerTrades = random.nextIntBetweenInclusive(min, max);
        for (int i = 0; i < 10 && set.size() < fillerTrades; i++) {
            set.add(random.nextInt(fillerOffers.size()));
        }
        Collection<MerchantOffer> offers = new ArrayList<>();
        for (Integer integer : set) {
            offers.add(fillerOffers.get(integer));
        }
        return offers;
    }

    @Override
    public void overrideOffers(MerchantOffers pOffers) {

    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isTrading();
    }

    @Override
    public void notifyTrade(MerchantOffer pOffer) {
        pOffer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        //this.rewardTradeXp(pOffer);
    }

    @Override
    public void notifyTradeUpdated(ItemStack pStack) {
        if (!this.level.isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
            this.playSound(this.getTradeUpdatedSound(!pStack.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
        }
    }

    protected SoundEvent getTradeUpdatedSound(boolean pIsYesSound) {
        return pIsYesSound ? SoundEvents.PIGLIN_ADMIRING_ITEM : SoundEvents.PIGLIN_JEALOUS;
    }

    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.PIGLIN_ADMIRING_ITEM;
    }

    @Override
    public Optional<SoundEvent> getAngerSound() {
        return Optional.of(SoundEvents.PIGLIN_BRUTE_ANGRY);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.PIGLIN_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.PIGLIN_STEP, 0.15F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        serializeMerchant(pCompound, this.offers, this.lastRestockGameTime, this.numberOfRestocksToday);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        deserializeMerchant(pCompound, c -> this.offers = c);
    }
}
