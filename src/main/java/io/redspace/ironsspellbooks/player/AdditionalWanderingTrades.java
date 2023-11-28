package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber
public class AdditionalWanderingTrades {

    //By default, wandering traders spawn with 5 random generic trades, and 1 random rare trade (6 trades total)
    public static final int INK_SALE_PRICE_PER_RARITY = 4;
    public static final int INK_BUY_PRICE_PER_RARITY = 2;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addWanderingTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> additionalGenericTrades = List.of(
                //ScrollMerchantOffer.createListing(SpellRarity.COMMON, 4, 6, 0, 0.05f),
                //ScrollMerchantOffer.createListing(SpellRarity.UNCOMMON, 6, 5, 0, 0.05f),
                //ScrollMerchantOffer.createListing(SpellRarity.RARE, 11, 4, 0, 0.05f),
                //ScrollMerchantOffer.createListing(SpellRarity.EPIC, 14, 3, 0, 0.05f),
                //ScrollMerchantOffer.createListing(SpellRarity.LEGENDARY, 18, 2, 0, 0.05f)
                //fuck it, add it three times
                new RandomScrollTrade(new SpellFilter()),
                new RandomScrollTrade(new SpellFilter()),
                new RandomScrollTrade(new SpellFilter()),
                //Buy back ink
                new InkBuyTrade((InkItem) ItemRegistry.INK_COMMON.get()),
                new InkBuyTrade((InkItem) ItemRegistry.INK_UNCOMMON.get()),
                new InkBuyTrade((InkItem) ItemRegistry.INK_RARE.get()),
                new InkBuyTrade((InkItem) ItemRegistry.INK_EPIC.get()),
                new InkBuyTrade((InkItem) ItemRegistry.INK_LEGENDARY.get()),
                new RandomCurioTrade()/*,
                SimpleTrade.of((trader, random) -> new MerchantOffer(
                        new ItemStack(Items.EMERALD, 64 - random.nextIntBetweenInclusive(1, 8)),
                        new ItemStack(Items.ECHO_SHARD, random.nextIntBetweenInclusive(1, 3)),
                        FurledMapItem.of(IronsSpellbooks.id("evoker_fort"), Component.translatable("item.irons_spellbooks.evoker_fort_battle_plans")),
                        8,
                        0,
                        .05f
                ))*/
        );
        List<VillagerTrades.ItemListing> additionalRareTrades = List.of(
                SimpleTrade.of((trader, random) -> new MerchantOffer(
                        new ItemStack(Items.EMERALD, 64 - random.nextIntBetweenInclusive(1, 8)),
                        new ItemStack(Items.ECHO_SHARD, random.nextIntBetweenInclusive(1, 3)),
                        new ItemStack(ItemRegistry.LOST_KNOWLEDGE_FRAGMENT.get()),
                        8,
                        0,
                        .05f
                )),
                //Add them multiple times to increase their likelihood of dropping.
                new RandomCurioTrade(),
                new RandomCurioTrade(),
                new RandomCurioTrade(),
                new ScrollPouchTrade(),
                new ScrollPouchTrade()
        );
        event.getGenericTrades().addAll(additionalGenericTrades);
        event.getRareTrades().addAll(additionalRareTrades);
    }

    static class SimpleTrade implements VillagerTrades.ItemListing {
        final BiFunction<Entity, RandomSource, MerchantOffer> getOffer;

        private SimpleTrade(BiFunction<Entity, RandomSource, MerchantOffer> getOffer) {
            this.getOffer = getOffer;
        }

        public static SimpleTrade of(BiFunction<Entity, RandomSource, MerchantOffer> getOffer) {
            return new SimpleTrade(getOffer);
        }

        public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
            return getOffer.apply(pTrader, pRandom);
        }
    }

    static class InkBuyTrade extends SimpleTrade {
        private InkBuyTrade(InkItem item) {
            super((trader, random) -> {
                //There is a 50% chance that the trader will give essence instead of emeralds. they give half as many essences as emeralds
                boolean emeralds = random.nextBoolean();
                return new MerchantOffer(
                        new ItemStack(item),
                        new ItemStack(emeralds ? Items.EMERALD : ItemRegistry.ARCANE_ESSENCE.get(), INK_BUY_PRICE_PER_RARITY * item.getRarity().getValue() / (emeralds ? 1 : 2) + random.nextIntBetweenInclusive(2, 3)),
                        8,
                        1,
                        .05f
                );
            });
        }
    }

    static class InkSellTrade extends SimpleTrade {
        private InkSellTrade(InkItem item) {
            super((trader, random) -> {
                return new MerchantOffer(
                        new ItemStack(Items.EMERALD, INK_SALE_PRICE_PER_RARITY * item.getRarity().getValue() + random.nextIntBetweenInclusive(2, 3)),
                        new ItemStack(item),
                        4,
                        1,
                        .05f
                );
            });
        }
    }

    static class RandomCurioTrade extends SimpleTrade {

        private RandomCurioTrade() {
            super((trader, random) -> {
                if (!trader.level.isClientSide) {
                    LootTable loottable = trader.level.getServer().getLootTables().get(IronsSpellbooks.id("magic_items/basic_curios"));
                    var context = new LootContext.Builder((ServerLevel) trader.level).create(LootContextParamSets.EMPTY);
                    var items = loottable.getRandomItems(context);
                    if (!items.isEmpty()) {
                        ItemStack forSale = items.get(0);
                        ItemStack cost = new ItemStack(Items.EMERALD, random.nextIntBetweenInclusive(14, 25));
                        return new MerchantOffer(cost, forSale, 1, 5, 0.5f);
                    }
                }
                return null;
            });
        }
    }

    static class ScrollPouchTrade extends SimpleTrade {
        private ScrollPouchTrade() {
            super((trader, random) -> {
                if (!trader.level.isClientSide) {
                    LootTable loottable = trader.level.getServer().getLootTables().get(IronsSpellbooks.id("magic_items/scroll_pouch"));
                    var context = new LootContext.Builder((ServerLevel) trader.level).create(LootContextParamSets.EMPTY);
                    var items = loottable.getRandomItems(context);
                    if (!items.isEmpty()) {
                        int quality = 0;
                        ItemStack forSale = new ItemStack(Items.BUNDLE).setHoverName(Component.translatable("item.irons_spellbooks.scroll_pouch"));
                        ListTag itemsTag = new ListTag();
                        for (ItemStack scroll : items) {
                            itemsTag.add(scroll.save(new CompoundTag()));
                            quality += SpellData.getSpellData(scroll).getRarity().getValue() + 1;
                        }
                        forSale.getOrCreateTag().put("Items", itemsTag);
                        ItemStack cost = new ItemStack(Items.EMERALD, quality * 4 + random.nextIntBetweenInclusive(8, 16));
                        return new MerchantOffer(cost, forSale, 1, 5, 0.5f);
                    }
                }
                return null;
            });
        }
    }

    static class RandomScrollTrade implements VillagerTrades.ItemListing {
        protected final ItemStack price;
        protected final ItemStack price2;
        protected final ItemStack forSale;
        protected final int maxTrades;
        protected final int xp;
        protected final float priceMult;
        protected final SpellFilter spellFilter;

        public RandomScrollTrade(SpellFilter spellFilter) {
            this.spellFilter = spellFilter;
            this.price = new ItemStack(Items.EMERALD);
            this.price2 = ItemStack.EMPTY;
            this.forSale = new ItemStack(ItemRegistry.SCROLL.get());
            this.maxTrades = 1;
            this.xp = 5;
            this.priceMult = .05f;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity pTrader, RandomSource random) {
            AbstractSpell spell = spellFilter.getRandomSpell(random);
            int level = random.nextIntBetweenInclusive(1, spell.getMaxLevel());
            SpellData.setSpellData(forSale, spell, level);
            this.price.setCount(spell.getRarity(level).getValue() * 5 + random.nextIntBetweenInclusive(4, 7));
            return new MerchantOffer(price, price2, forSale, maxTrades, xp, priceMult);
        }
    }

    static class ScrollMerchantOffer extends MerchantOffer {
        final SpellRarity scrollRarity;

        public ScrollMerchantOffer(SpellRarity scrollRarity, int emeralds, int pMaxUses, int pXp, float pPriceMultiplier) {
            super(new ItemStack(ItemRegistry.SCROLL.get()).setHoverName(Component.translatable("ui.irons_spellbooks.wandering_trader_scroll", scrollRarity.getDisplayName())), new ItemStack(Items.EMERALD, emeralds), pMaxUses, pXp, pPriceMultiplier);
            this.scrollRarity = scrollRarity;
        }

        @Override
        public boolean satisfiedBy(ItemStack offerA, ItemStack offerB) {
            return offerA.is(ItemRegistry.SCROLL.get()) && SpellData.getSpellData(offerA).getRarity() == scrollRarity && offerA.getCount() >= this.getCostA().getCount() &&
                    this.isRequiredItem(offerB, this.getCostB()) && offerB.getCount() >= this.getCostB().getCount();
        }

        private boolean isRequiredItem(ItemStack pOffer, ItemStack pCost) {
            if (pCost.isEmpty() && pOffer.isEmpty()) {
                return true;
            } else {
                ItemStack itemstack = pOffer.copy();
                if (itemstack.getItem().isDamageable(itemstack)) {
                    itemstack.setDamageValue(itemstack.getDamageValue());
                }

                return ItemStack.isSame(itemstack, pCost) && (!pCost.hasTag() || itemstack.hasTag() && NbtUtils.compareNbt(pCost.getTag(), itemstack.getTag(), false));
            }
        }

        static VillagerTrades.ItemListing createListing(SpellRarity scrollRarity, int emeralds, int pMaxUses, int pXp, float pPriceMultiplier) {
            return new VillagerTrades.ItemListing() {
                @Nullable
                @Override
                public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
                    return new ScrollMerchantOffer(scrollRarity, emeralds, pMaxUses, pXp, pPriceMultiplier);
                }
            };
        }
    }
}
