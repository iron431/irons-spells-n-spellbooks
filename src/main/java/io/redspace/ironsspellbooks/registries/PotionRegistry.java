package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PotionRegistry {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
        eventBus.addListener(PotionRegistry::addRecipes);
        //IronsSpellbooks.LOGGER.debug("registering potions");
    }

    public static final DeferredHolder<Potion, Potion> INSTANT_MANA_ONE = POTIONS.register("instant_mana_one", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA)));
    public static final DeferredHolder<Potion, Potion> INSTANT_MANA_TWO = POTIONS.register("instant_mana_two", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA, 0, 1)));
    public static final DeferredHolder<Potion, Potion> INSTANT_MANA_THREE = POTIONS.register("instant_mana_three", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA, 0, 2)));
    public static final DeferredHolder<Potion, Potion> INSTANT_MANA_FOUR = POTIONS.register("instant_mana_four", () -> new Potion("mana", new MobEffectInstance(MobEffectRegistry.INSTANT_MANA, 0, 3)));

    public static void addRecipes(RegisterBrewingRecipesEvent event) {
        //IronsSpellbooks.LOGGER.debug("adding potion recipes");

            event.getBuilder().addMix(Potions.AWKWARD, ItemRegistry.ARCANE_ESSENCE.get(), PotionRegistry.INSTANT_MANA_ONE);
            event.getBuilder().addMix(PotionRegistry.INSTANT_MANA_ONE, Items.GLOWSTONE_DUST, PotionRegistry.INSTANT_MANA_TWO);
            event.getBuilder().addMix(PotionRegistry.INSTANT_MANA_TWO, Items.AMETHYST_SHARD, PotionRegistry.INSTANT_MANA_THREE);
            event.getBuilder().addMix(PotionRegistry.INSTANT_MANA_THREE, Items.AMETHYST_CLUSTER, PotionRegistry.INSTANT_MANA_FOUR);
    }
}
