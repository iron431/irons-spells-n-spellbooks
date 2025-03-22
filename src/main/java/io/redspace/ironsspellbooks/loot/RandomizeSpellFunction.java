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

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() instanceof Scroll || Utils.canImbue(itemStack)) {
            ItemStack fallback = itemStack.getItem() instanceof Scroll ? ItemStack.EMPTY : itemStack;
            var applicableSpells = this.applicableSpells.getApplicableSpells();
            if (applicableSpells.isEmpty()) {
                return fallback;
            }
            var spellList = getWeightedSpellList(applicableSpells);
            int total = spellList.floorKey(Integer.MAX_VALUE);
            AbstractSpell spell = spellList.higherEntry(lootContext.getRandom().nextInt(total)).getValue();
            if (spell.equals(SpellRegistry.none())) {
                return fallback;
            }

            int maxLevel = spell.getMaxLevel();
            float quality = qualityRange.getFloat(lootContext);
            //https://www.desmos.com/calculator/ablc1wg06w
            //quality = quality * Mth.sin(Mth.HALF_PI * quality);
            int spellLevel = 1 + Math.round(quality * (maxLevel - 1));
            if (itemStack.getItem() instanceof Scroll) {
                ISpellContainer.createScrollContainer(spell, spellLevel, itemStack);
            } else {
                ISpellContainer.createImbuedContainer(spell, spellLevel, itemStack);
            }
        }
        return itemStack;
    }

    private NavigableMap<Integer, AbstractSpell> getWeightedSpellList(List<AbstractSpell> entries) {
        int total = 0;
        NavigableMap<Integer, AbstractSpell> weightedSpells = new TreeMap<>();

        for (AbstractSpell entry : entries) {
            total += getWeightFromRarity(SpellRarity.values()[entry.getMinRarity()]);
            weightedSpells.put(total, entry);

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
