package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import software.bernie.geckolib.GeckoLib;


@Mod.EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabRegistry {

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    public static final RegistryObject<CreativeModeTab> EQUIPMENT_TAB = TABS.register("spellbook_equipment", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + IronsSpellbooks.MODID + ".spell_equipment_tab"))
            .icon(() -> new ItemStack(ItemRegistry.IRON_SPELL_BOOK.get()))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(ItemRegistry.NETHERITE_SPELL_BOOK.get());
                entries.accept(ItemRegistry.DIAMOND_SPELL_BOOK.get());
                entries.accept(ItemRegistry.GOLD_SPELL_BOOK.get());
                entries.accept(ItemRegistry.IRON_SPELL_BOOK.get());
                entries.accept(ItemRegistry.COPPER_SPELL_BOOK.get());
                entries.accept(ItemRegistry.EVOKER_SPELL_BOOK.get());
                entries.accept(ItemRegistry.ROTTEN_SPELL_BOOK.get());
                entries.accept(ItemRegistry.BLAZE_SPELL_BOOK.get());
                entries.accept(ItemRegistry.DRAGONSKIN_SPELL_BOOK.get());
                entries.accept(ItemRegistry.VILLAGER_SPELL_BOOK.get());
                entries.accept(ItemRegistry.BLOOD_STAFF.get());
                entries.accept(ItemRegistry.MAGEHUNTER.get());

                entries.accept(ItemRegistry.WANDERING_MAGICIAN_HELMET.get());
                entries.accept(ItemRegistry.WANDERING_MAGICIAN_CHESTPLATE.get());
                entries.accept(ItemRegistry.WANDERING_MAGICIAN_LEGGINGS.get());
                entries.accept(ItemRegistry.WANDERING_MAGICIAN_BOOTS.get());
                entries.accept(ItemRegistry.PUMPKIN_HELMET.get());
                entries.accept(ItemRegistry.PUMPKIN_CHESTPLATE.get());
                entries.accept(ItemRegistry.PUMPKIN_LEGGINGS.get());
                entries.accept(ItemRegistry.PUMPKIN_BOOTS.get());
                entries.accept(ItemRegistry.PYROMANCER_HELMET.get());
                entries.accept(ItemRegistry.PYROMANCER_CHESTPLATE.get());
                entries.accept(ItemRegistry.PYROMANCER_LEGGINGS.get());
                entries.accept(ItemRegistry.PYROMANCER_BOOTS.get());
                entries.accept(ItemRegistry.ELECTROMANCER_HELMET.get());
                entries.accept(ItemRegistry.ELECTROMANCER_CHESTPLATE.get());
                entries.accept(ItemRegistry.ELECTROMANCER_LEGGINGS.get());
                entries.accept(ItemRegistry.ELECTROMANCER_BOOTS.get());
                entries.accept(ItemRegistry.ARCHEVOKER_HELMET.get());
                entries.accept(ItemRegistry.ARCHEVOKER_CHESTPLATE.get());
                entries.accept(ItemRegistry.ARCHEVOKER_LEGGINGS.get());
                entries.accept(ItemRegistry.ARCHEVOKER_BOOTS.get());
                entries.accept(ItemRegistry.CULTIST_HELMET.get());
                entries.accept(ItemRegistry.CULTIST_CHESTPLATE.get());
                entries.accept(ItemRegistry.CULTIST_LEGGINGS.get());
                entries.accept(ItemRegistry.CULTIST_BOOTS.get());
                entries.accept(ItemRegistry.CRYOMANCER_HELMET.get());
                entries.accept(ItemRegistry.CRYOMANCER_CHESTPLATE.get());
                entries.accept(ItemRegistry.CRYOMANCER_LEGGINGS.get());
                entries.accept(ItemRegistry.CRYOMANCER_BOOTS.get());
                entries.accept(ItemRegistry.SHADOWWALKER_HELMET.get());
                entries.accept(ItemRegistry.SHADOWWALKER_CHESTPLATE.get());
                entries.accept(ItemRegistry.SHADOWWALKER_LEGGINGS.get());
                entries.accept(ItemRegistry.SHADOWWALKER_BOOTS.get());
                entries.accept(ItemRegistry.PRIEST_HELMET.get());
                entries.accept(ItemRegistry.PRIEST_CHESTPLATE.get());
                entries.accept(ItemRegistry.PRIEST_LEGGINGS.get());
                entries.accept(ItemRegistry.PRIEST_BOOTS.get());
                entries.accept(ItemRegistry.PLAGUED_HELMET.get());
                entries.accept(ItemRegistry.PLAGUED_CHESTPLATE.get());
                entries.accept(ItemRegistry.PLAGUED_LEGGINGS.get());
                entries.accept(ItemRegistry.PLAGUED_BOOTS.get());
                entries.accept(ItemRegistry.TARNISHED_CROWN.get());

                //TODO: 1.20 port: redo curios
//                entries.accept(ItemRegistry.MANA_RING.get());
//                entries.accept(ItemRegistry.SILVER_RING.get());
//                entries.accept(ItemRegistry.COOLDOWN_RING.get());
//                entries.accept(ItemRegistry.CAST_TIME_RING.get());
//                entries.accept(ItemRegistry.HEAVY_CHAIN.get());
                entries.accept(ItemRegistry.EMERALD_STONEPLATE_RING.get());
//                entries.accept(ItemRegistry.FIREWARD_RING.get());
//                entries.accept(ItemRegistry.FROSTWARD_RING.get());
//                entries.accept(ItemRegistry.POISONWARD_RING.get());
//                entries.accept(ItemRegistry.CONJURERS_TALISMAN.get());
//                entries.accept(ItemRegistry.CASTER_RING.get());

                entries.accept(ItemRegistry.WAYWARD_COMPASS.get());
            })
            .build());

    public static final RegistryObject<CreativeModeTab> MATERIALS_TAB = TABS.register("spellbook_materials", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + IronsSpellbooks.MODID + ".spell_materials_tab"))
            .icon(() -> new ItemStack(ItemRegistry.DIVINE_PEARL.get()))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(ItemRegistry.INK_COMMON.get());
                entries.accept(ItemRegistry.INK_UNCOMMON.get());
                entries.accept(ItemRegistry.INK_RARE.get());
                entries.accept(ItemRegistry.INK_EPIC.get());
                entries.accept(ItemRegistry.INK_LEGENDARY.get());

                entries.accept(ItemRegistry.UPGRADE_ORB.get());
                entries.accept(ItemRegistry.FIRE_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.ICE_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.LIGHTNING_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.HOLY_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.ENDER_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.BLOOD_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.EVOCATION_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.POISON_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.MANA_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.COOLDOWN_UPGRADE_ORB.get());
                entries.accept(ItemRegistry.PROTECTION_UPGRADE_ORB.get());

                entries.accept(ItemRegistry.LIGHTNING_BOTTLE.get());
                entries.accept(ItemRegistry.FROZEN_BONE_SHARD.get());
                entries.accept(ItemRegistry.BLOOD_VIAL.get());
                entries.accept(ItemRegistry.DIVINE_PEARL.get());

                entries.accept(ItemRegistry.HOGSKIN.get());
                entries.accept(ItemRegistry.DRAGONSKIN.get());
                entries.accept(ItemRegistry.ARCANE_ESSENCE.get());
                entries.accept(ItemRegistry.MAGIC_CLOTH.get());
                entries.accept(ItemRegistry.RUINED_BOOK.get());
                entries.accept(ItemRegistry.CINDER_ESSENCE.get());
                entries.accept(ItemRegistry.ARCANE_SALVAGE.get());
                entries.accept(ItemRegistry.ARCANE_INGOT.get());
                entries.accept(ItemRegistry.SHRIVING_STONE.get());

                entries.accept(ItemRegistry.BLANK_RUNE.get());
                entries.accept(ItemRegistry.FIRE_RUNE.get());
                entries.accept(ItemRegistry.ICE_RUNE.get());
                entries.accept(ItemRegistry.LIGHTNING_RUNE.get());
                entries.accept(ItemRegistry.ENDER_RUNE.get());
                entries.accept(ItemRegistry.HOLY_RUNE.get());
                entries.accept(ItemRegistry.BLOOD_RUNE.get());
                entries.accept(ItemRegistry.EVOCATION_RUNE.get());
                entries.accept(ItemRegistry.MANA_RUNE.get());
                entries.accept(ItemRegistry.COOLDOWN_RUNE.get());
                entries.accept(ItemRegistry.PROTECTION_RUNE.get());
                entries.accept(ItemRegistry.POISON_RUNE.get());

                entries.accept(ItemRegistry.KEEPER_SPAWN_EGG.get());
                entries.accept(ItemRegistry.DEAD_KING_CORPSE_SPAWN_EGG.get());
                entries.accept(ItemRegistry.ARCHEVOKER_SPAWN_EGG.get());
                entries.accept(ItemRegistry.NECROMANCER_SPAWN_EGG.get());
                entries.accept(ItemRegistry.CRYOMANCER_SPAWN_EGG.get());
                entries.accept(ItemRegistry.PYROMANCER_SPAWN_EGG.get());
            })
            .build());

    @SubscribeEvent
    public static void fillCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(ItemRegistry.INSCRIPTION_TABLE_BLOCK_ITEM.get());
            event.accept(ItemRegistry.SCROLL_FORGE_BLOCK.get());
            event.accept(ItemRegistry.ACANE_ANVIL_BLOCK_ITEM.get());
            event.accept(ItemRegistry.PEDESTAL_BLOCK_ITEM.get());
            event.accept(ItemRegistry.ARMOR_PILE_BLOCK_ITEM.get());
        }

        if (event.getTab() == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.NATURAL_BLOCKS)) {
            event.accept(ItemRegistry.ARCANE_DEBRIS_BLOCK_ITEM.get());
        }

    }
}
