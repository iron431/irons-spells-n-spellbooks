package io.redspace.ironsspellbooks.registries;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.loot.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;


public class LootRegistry {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, IronsSpellbooks.MODID);
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        LOOT_FUNCTIONS.register(eventBus);
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }

    public static final Supplier<LootItemFunctionType<?>> RANDOMIZE_SPELL_FUNCTION = LOOT_FUNCTIONS.register("randomize_spell",
            () -> new LootItemFunctionType<>(RandomizeSpellFunction.CODEC));
    public static final Supplier<LootItemFunctionType<?>> RANDOMIZE_SPELL_RING_FUNCTION = LOOT_FUNCTIONS.register("randomize_ring_enhancement",
            () -> new LootItemFunctionType<>(RandomizeRingEnhancementFunction.CODEC));
    public static final Supplier<LootItemFunctionType<?>> SET_FURLED_MAP_FUNCTION = LOOT_FUNCTIONS.register("set_furled_map",
            () -> new LootItemFunctionType<>(FurledMapLootFunction.CODEC));

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> APPEND_LOOT_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("append_loot", AppendLootModifier.CODEC);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> REPLACE_LOOT_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("replace_loot", ReplaceLootModifier.CODEC);
}
