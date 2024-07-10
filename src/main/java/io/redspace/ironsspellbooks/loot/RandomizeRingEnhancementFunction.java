package io.redspace.ironsspellbooks.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.item.curios.AffinityRing;
import io.redspace.ironsspellbooks.registries.LootRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class RandomizeRingEnhancementFunction extends LootItemConditionalFunction {
    protected RandomizeRingEnhancementFunction(List<LootItemCondition> lootConditions, SpellFilter spellFilter) {
        super(lootConditions);
        this.spellFilter = spellFilter;
    }

    final SpellFilter spellFilter;

    public static final MapCodec<RandomizeRingEnhancementFunction> CODEC = RecordCodecBuilder.mapCodec(builder -> commonFields(builder).and(
            SpellFilter.CODEC.fieldOf("spell_filter").forGetter(data -> data.spellFilter)
    ).apply(builder, RandomizeRingEnhancementFunction::new));

//    public static LootItemConditionalFunction.Builder<?> create(final SpellFilter filter) {
//        return simpleBuilder((functions) -> new RandomizeRingEnhancementFunction(functions, filter));
//    }
//
//    public static LootItemConditionalFunction.Builder<?> allSpells() {
//        return simpleBuilder((functions) -> new RandomizeRingEnhancementFunction(functions, new SpellFilter()));
//    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        //irons_spellbooks.LOGGER.debug("RandomizeScrollFunction.run {}", itemStack.hashCode());
        if (itemStack.getItem() instanceof AffinityRing) {
            var spell = spellFilter.getRandomSpell(lootContext.getRandom(), (s) -> s.getMaxLevel() > 1);
            AffinityData.setAffinityData(itemStack, spell);
            return spell == SpellRegistry.none() ? ItemStack.EMPTY : itemStack;
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RANDOMIZE_SPELL_RING_FUNCTION.get();
    }

}
