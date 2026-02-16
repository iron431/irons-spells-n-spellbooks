package io.redspace.ironsspellbooks.entity.mobs.wizards.cryomancer;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import io.redspace.ironsspellbooks.entity.mobs.wizards.IMerchantWizard;
import io.redspace.ironsspellbooks.item.FurledMapItem;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import io.redspace.ironsspellbooks.player.AdditionalWanderingTrades;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.*;

public class CryomancerEntity  extends NeutralWizard implements IMerchantWizard {

    public CryomancerEntity(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 25;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FocusOnTradingPlayerGoal<>(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SpellBarrageGoal(this, SpellRegistry.ICE_BLOCK_SPELL.get(), 3, 6, 100, 250, 1));
        this.goalSelector.addGoal(3, new WizardAttackGoal(this, 1.25f, 50, 75)
                .setSpells(
                        List.of(SpellRegistry.ICICLE_SPELL.get(), SpellRegistry.ICICLE_SPELL.get(), SpellRegistry.ICICLE_SPELL.get(), SpellRegistry.CONE_OF_COLD_SPELL.get()),
                        List.of(SpellRegistry.COUNTERSPELL_SPELL.get()),
                        List.of(SpellRegistry.FROST_STEP_SPELL.get()),
                        List.of()
                ).setSingleUseSpell(SpellRegistry.SUMMON_POLAR_BEAR_SPELL.get(), 80, 400, 3, 6)
                .setDrinksPotions()
        );
        this.goalSelector.addGoal(4, new PatrolNearLocationGoal(this, 30, .75f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new WizardRecoverGoal(this));

        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isHostileTowards));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData) {
        RandomSource randomsource = Utils.random;
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.CRYOMANCER_HELMET.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.CRYOMANCER_CHESTPLATE.get()));
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, .25);
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
        boolean preventTrade = isAggressive() || (!this.level.isClientSide && this.getOffers().isEmpty());
        if (!preventTrade) {
            if (!this.level.isClientSide && !this.getOffers().isEmpty()) {
                if (shouldRestock()) {
                    restock();
                }
                this.startTrading(pPlayer);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.mobInteract(pPlayer, pHand);
    }

    private void startTrading(Player pPlayer) {
        this.setTradingPlayer(pPlayer);
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
    public Level level() {
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

            this.offers.addAll(createRandomOffers(2, 3));

            if (this.random.nextFloat() < 0.25f) {
                this.offers.add(new AdditionalWanderingTrades.InkBuyTrade((InkItem) ItemRegistry.INK_COMMON.get()).getOffer(this, this.random));
            }
            if (this.random.nextFloat() < 0.25f) {
                this.offers.add(new AdditionalWanderingTrades.InkBuyTrade((InkItem) ItemRegistry.INK_UNCOMMON.get()).getOffer(this, this.random));
            }
            if (this.random.nextFloat() < 0.25f) {
                this.offers.add(new AdditionalWanderingTrades.InkBuyTrade((InkItem) ItemRegistry.INK_RARE.get()).getOffer(this, this.random));
            }

            this.offers.add(new AdditionalWanderingTrades.RandomScrollTrade(new SpellFilter(SchoolRegistry.ICE.get()), 0f, .25f).getOffer(this, this.random));
            if (this.random.nextFloat() < .8f) {
                this.offers.add(new AdditionalWanderingTrades.RandomScrollTrade(new SpellFilter(SchoolRegistry.ICE.get()), .3f, .7f).getOffer(this, this.random));
            }
            if (this.random.nextFloat() < .8f) {
                this.offers.add(new AdditionalWanderingTrades.RandomScrollTrade(new SpellFilter(SchoolRegistry.ICE.get()), .8f, 1f).getOffer(this, this.random));
            }
            this.offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 32),
                    Optional.empty(),
                    //todo: replace with dedicated ice spider map
                    random.nextBoolean() ? FurledMapItem.of(IronsSpellbooks.id("impaled_icebreaker"), Component.translatable("item.irons_spellbooks.failed_arctic_voyage_map")) :
                            FurledMapItem.of(IronsSpellbooks.id("ice_spider_den"), Component.translatable("item.irons_spellbooks.ice_spider_den_map")),
                    0,
                    1,
                    5,
                    10f
            ));

            this.offers.add(new MerchantOffer(
                    new ItemCost(ItemRegistry.FIRE_ALE.get(), 4),
                    Optional.empty(),
                    ItemRegistry.MUSIC_DISC_WHISPERS_OF_ICE.get().getDefaultInstance(),
                    0,
                    1,
                    5,
                    0.1f
            ));
            this.offers.add(new MerchantOffer(
                    new ItemCost(ItemRegistry.ICY_FANG.get(), 2),
                    Optional.empty(),
                    ItemRegistry.ICE_RUNE.get().getDefaultInstance(),
                    0,
                    1,
                    5,
                    0.1f
            ));
            this.offers.removeIf(Objects::isNull);
            //We count the creation of our stock as a restock so that we do not immediately refresh trades the same day.
            setLastRestockGameTime(level.getGameTime());
        }
        return this.offers;
    }

    private static final List<VillagerTrades.ItemListing> fillerOffers = List.of(
//            new AdditionalWanderingTrades.SimpleBuy(16, new ItemCost(Items.CANDLE, 4), 1, 1),
//            new AdditionalWanderingTrades.SimpleSell(8, new ItemStack(Items.CANDLE, 4), 10, 14),
//            new AdditionalWanderingTrades.SimpleSell(8, new ItemStack(Items.FIRE_CHARGE, 3), 9, 13),
//            new AdditionalWanderingTrades.SimpleSell(12, new ItemStack(Items.LANTERN, 3), 6, 10),
//            new AdditionalWanderingTrades.SimpleBuy(16, new ItemCost(Items.HONEY_BOTTLE, 2), 3, 5),
//            new AdditionalWanderingTrades.SimpleBuy(16, new ItemCost(Items.BLAZE_ROD, 3), 4, 6)
    );

    private Collection<MerchantOffer> createRandomOffers(int min, int max) {
//        Set<Integer> set = Sets.newHashSet();
//        int fillerTrades = random.nextIntBetweenInclusive(min, max);
//        for (int i = 0; i < 10 && set.size() < fillerTrades; i++) {
//            set.add(random.nextInt(fillerOffers.size()));
//        }
        Collection<MerchantOffer> offers = new ArrayList<>();
//        for (Integer integer : set) {
//            offers.add(fillerOffers.get(integer).getOffer(this, this.random));
//        }
        return offers;
    }

    @Override
    public void overrideOffers(MerchantOffers pOffers) {

    }

    @Override
    public void notifyTrade(MerchantOffer pOffer) {
        pOffer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    public void notifyTradeUpdated(ItemStack pStack) {
        if (!this.level.isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
            this.playSound(this.getTradeUpdatedSound(!pStack.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
        }
    }

    protected SoundEvent getTradeUpdatedSound(boolean pIsYesSound) {
        return pIsYesSound ? SoundRegistry.TRADER_YES.get() : SoundRegistry.TRADER_NO.get();
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundRegistry.TRADER_YES.get();
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
