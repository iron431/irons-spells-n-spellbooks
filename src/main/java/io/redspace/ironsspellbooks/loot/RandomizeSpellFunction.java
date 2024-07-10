package io.redspace.ironsspellbooks.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class RandomizeSpellFunction extends LootItemConditionalFunction {
    final NumberProvider qualityRange;
    final SpellFilter applicableSpells;

    protected RandomizeSpellFunction(List<LootItemCondition> lootConditions, NumberProvider qualityRange, SpellFilter spellFilter) {
        super(lootConditions);
        this.qualityRange = qualityRange;
        this.applicableSpells = spellFilter;
    }

    public static final MapCodec<RandomizeSpellFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> commonFields(builder).and(
            builder.group(
                    NumberProviders.CODEC.fieldOf("quality").forGetter(data -> data.qualityRange),
                    SpellFilter.CODEC.optionalFieldOf("spell_filter", new SpellFilter()).forGetter(data -> data.applicableSpells)
            )
    ).apply(builder, RandomizeSpellFunction::new));
//    public static LootItemConditionalFunction.Builder<?> create(final NumberProvider quality, final SpellFilter filter) {
//        return simpleBuilder((functions) -> new RandomizeSpellFunction(functions, quality, filter));
//    }
//
//    public static LootItemConditionalFunction.Builder<?> allSpells(final NumberProvider quality) {
//        return simpleBuilder((functions) -> new RandomizeSpellFunction(functions, quality, new SpellFilter()));
//    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        //irons_spellbooks.LOGGER.debug("RandomizeScrollFunction.run {}", itemStack.hashCode());
        if (itemStack.getItem() instanceof Scroll || Utils.canImbue(itemStack)) {
            var applicableSpells = this.applicableSpells.getApplicableSpells();
            if (applicableSpells.isEmpty()) {
                //Return empty item stack
                itemStack.setCount(0);
                return itemStack;
            }
            var spellList = getWeightedSpellList(applicableSpells);
            int total = spellList.floorKey(Integer.MAX_VALUE);
            AbstractSpell abstractSpell = SpellRegistry.none();
            if (!spellList.isEmpty()) {
                abstractSpell = spellList.higherEntry(lootContext.getRandom().nextInt(total)).getValue();
            }

            int maxLevel = abstractSpell.getMaxLevel();
            float quality = qualityRange.getFloat(lootContext);
            //https://www.desmos.com/calculator/ablc1wg06w
            //quality = quality * (float) Math.sin(1.57 * quality * quality);
            int spellLevel = 1 + Math.round(quality * (maxLevel - 1));

            if (itemStack.getItem() instanceof Scroll) {
                ISpellContainer.createScrollContainer(abstractSpell, spellLevel, itemStack);
            } else {
                ISpellContainer.createImbuedContainer(abstractSpell, spellLevel, itemStack);
            }
        }
        return itemStack;
    }

    private NavigableMap<Integer, AbstractSpell> getWeightedSpellList(List<AbstractSpell> entries) {
        int total = 0;
        NavigableMap<Integer, AbstractSpell> weightedSpells = new TreeMap<>();

        for (AbstractSpell entry : entries) {
            if (entry != SpellRegistry.none() && entry.isEnabled()) {
                total += getWeightFromRarity(SpellRarity.values()[entry.getMinRarity()]);
                weightedSpells.put(total, entry);
            }
        }

        return weightedSpells;
    }

    private int getWeightFromRarity(SpellRarity rarity) {
        return switch (rarity) {
            case COMMON -> 40;
            case UNCOMMON -> 30;
            case RARE -> 15;
            case EPIC -> 8;
            case LEGENDARY -> 4;
        };
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RANDOMIZE_SPELL_FUNCTION.get();
    }
}
