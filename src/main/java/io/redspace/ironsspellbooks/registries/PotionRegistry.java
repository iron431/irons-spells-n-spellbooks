package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.List;
import java.util.function.Supplier;

public class PotionRegistry {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
        eventBus.addListener(PotionRegistry::addRecipes);
        //IronsSpellbooks.LOGGER.debug("registering potions");
    }

    public static final Supplier<Potion> INSTANT_MANA_ONE = POTIONS.register("instant_mana_one", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA.get())));
    public static final Supplier<Potion> INSTANT_MANA_TWO = POTIONS.register("instant_mana_two", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA.get(), 0, 1)));
    public static final Supplier<Potion> INSTANT_MANA_THREE = POTIONS.register("instant_mana_three", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA.get(), 0, 2)));
    public static final Supplier<Potion> INSTANT_MANA_FOUR = POTIONS.register("instant_mana_four", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA.get(), 0, 3)));

    public static void addRecipes(FMLCommonSetupEvent event) {
        //IronsSpellbooks.LOGGER.debug("adding potion recipes");

        event.enqueueWork(() -> {
            PotionBrewing.addMix(Potions.AWKWARD, ItemRegistry.ARCANE_ESSENCE.get(), PotionRegistry.INSTANT_MANA_ONE.get());
            PotionBrewing.addMix(PotionRegistry.INSTANT_MANA_ONE.get(), Items.GLOWSTONE_DUST, PotionRegistry.INSTANT_MANA_TWO.get());
            PotionBrewing.addMix(PotionRegistry.INSTANT_MANA_TWO.get(), Items.AMETHYST_SHARD, PotionRegistry.INSTANT_MANA_THREE.get());
            PotionBrewing.addMix(PotionRegistry.INSTANT_MANA_THREE.get(), Items.AMETHYST_CLUSTER, PotionRegistry.INSTANT_MANA_FOUR.get());
//            addContainerMix(ItemRegistry.BLOOD_VIAL.get(), ItemRegistry.HOGSKIN.get(), ItemRegistry.ARCANE_ESSENCE.get());
        });
    }

    public static void addContainerMix(Item pFrom, Item pIngredient, Item pTo) {
        PotionBrewing.CONTAINER_MIXES.add(new PotionBrewing.Mix<>(net.minecraftforge.registries.ForgeRegistries.ITEMS, pFrom, Ingredient.of(pIngredient), pTo));
    }
}
