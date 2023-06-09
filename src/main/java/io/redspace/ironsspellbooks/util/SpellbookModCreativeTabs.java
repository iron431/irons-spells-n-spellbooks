package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import software.bernie.geckolib.GeckoLib;

public class SpellbookModCreativeTabs {
    //    public static final CreativeModeTab SPELL_MATERIALS_TAB = new CreativeModeTab("spell_materials_tab") {
//        @Override
//        public ItemStack makeIcon() {
//            return new ItemStack(ItemRegistry.DIVINE_PEARL.get());
//        }
//    };
//    public static final CreativeModeTab SPELL_EQUIPMENT_TAB = new CreativeModeTab("spell_equipment_tab") {
//        @Override
//        public ItemStack makeIcon() {
//            return new ItemStack(ItemRegistry.IRON_SPELL_BOOK.get());
//        }
//    };
    // static final CreativeModeTab SPELL_MATERIALS_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title(Component.translatable("itemGroup.spell_equipment_tab")).icon(() -> new ItemStack(ItemRegistry.DIVINE_PEARL.get())).build();

    //TODO: 1.20 port
    public static void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
//        event.registerCreativeModeTab(IronsSpellbooks.id("spell_materials_tab"),
//                e -> e.icon(() -> new ItemStack(ItemRegistry.DIVINE_PEARL.get()))
//                        .title(Component.translatable("itemGroup." + IronsSpellbooks.MODID + ".spell_materials_tab"))
//                        .displayItems((enabledFeatures, entries) -> {
//                            entries.accept(ItemRegistry.INK_COMMON.get());
//                            entries.accept(ItemRegistry.INK_UNCOMMON.get());
//                            entries.accept(ItemRegistry.INK_RARE.get());
//                            entries.accept(ItemRegistry.INK_EPIC.get());
//                            entries.accept(ItemRegistry.INK_LEGENDARY.get());
//
//                            entries.accept(ItemRegistry.UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.FIRE_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.ICE_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.LIGHTNING_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.HOLY_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.ENDER_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.BLOOD_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.EVOCATION_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.POISON_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.MANA_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.COOLDOWN_UPGRADE_ORB.get());
//                            entries.accept(ItemRegistry.PROTECTION_UPGRADE_ORB.get());
//
//                            entries.accept(ItemRegistry.LIGHTNING_BOTTLE.get());
//                            entries.accept(ItemRegistry.FROZEN_BONE_SHARD.get());
//                            entries.accept(ItemRegistry.BLOOD_VIAL.get());
//                            entries.accept(ItemRegistry.DIVINE_PEARL.get());
//
//                            entries.accept(ItemRegistry.HOGSKIN.get());
//                            entries.accept(ItemRegistry.DRAGONSKIN.get());
//                            entries.accept(ItemRegistry.ARCANE_ESSENCE.get());
//                            entries.accept(ItemRegistry.MAGIC_CLOTH.get());
//                            entries.accept(ItemRegistry.RUINED_BOOK.get());
//                            entries.accept(ItemRegistry.CINDER_ESSENCE.get());
//                            entries.accept(ItemRegistry.ARCANE_SALVAGE.get());
//                            entries.accept(ItemRegistry.ARCANE_INGOT.get());
//                            entries.accept(ItemRegistry.SHRIVING_STONE.get());
//
//                            entries.accept(ItemRegistry.BLANK_RUNE.get());
//                            entries.accept(ItemRegistry.FIRE_RUNE.get());
//                            entries.accept(ItemRegistry.ICE_RUNE.get());
//                            entries.accept(ItemRegistry.LIGHTNING_RUNE.get());
//                            entries.accept(ItemRegistry.ENDER_RUNE.get());
//                            entries.accept(ItemRegistry.HOLY_RUNE.get());
//                            entries.accept(ItemRegistry.BLOOD_RUNE.get());
//                            entries.accept(ItemRegistry.EVOCATION_RUNE.get());
//                            entries.accept(ItemRegistry.MANA_RUNE.get());
//                            entries.accept(ItemRegistry.COOLDOWN_RUNE.get());
//                            entries.accept(ItemRegistry.PROTECTION_RUNE.get());
//                            entries.accept(ItemRegistry.POISON_RUNE.get());
//
//                            entries.accept(ItemRegistry.KEEPER_SPAWN_EGG.get());
//                            entries.accept(ItemRegistry.DEAD_KING_CORPSE_SPAWN_EGG.get());
//                            entries.accept(ItemRegistry.ARCHEVOKER_SPAWN_EGG.get());
//                            entries.accept(ItemRegistry.NECROMANCER_SPAWN_EGG.get());
//                            entries.accept(ItemRegistry.CRYOMANCER_SPAWN_EGG.get());
//                            entries.accept(ItemRegistry.PYROMANCER_SPAWN_EGG.get());
//
//                        }));
//
//        event.registerCreativeModeTab(IronsSpellbooks.id("spell_equipment_tab"),
//                e -> e.icon(() -> new ItemStack(ItemRegistry.IRON_SPELL_BOOK.get()))
//                        .title(Component.translatable("itemGroup." + IronsSpellbooks.MODID + ".spell_equipment_tab"))
//                        .displayItems((enabledFeatures, entries) -> {
//                            entries.accept(ItemRegistry.NETHERITE_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.DIAMOND_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.GOLD_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.IRON_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.COPPER_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.EVOKER_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.ROTTEN_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.BLAZE_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.DRAGONSKIN_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.VILLAGER_SPELL_BOOK.get());
//                            entries.accept(ItemRegistry.BLOOD_STAFF.get());
//                            entries.accept(ItemRegistry.MAGEHUNTER.get());
//
//                            entries.accept(ItemRegistry.WANDERING_MAGICIAN_HELMET.get());
//                            entries.accept(ItemRegistry.WANDERING_MAGICIAN_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.WANDERING_MAGICIAN_LEGGINGS.get());
//                            entries.accept(ItemRegistry.WANDERING_MAGICIAN_BOOTS.get());
//                            entries.accept(ItemRegistry.PUMPKIN_HELMET.get());
//                            entries.accept(ItemRegistry.PUMPKIN_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.PUMPKIN_LEGGINGS.get());
//                            entries.accept(ItemRegistry.PUMPKIN_BOOTS.get());
//                            entries.accept(ItemRegistry.PYROMANCER_HELMET.get());
//                            entries.accept(ItemRegistry.PYROMANCER_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.PYROMANCER_LEGGINGS.get());
//                            entries.accept(ItemRegistry.PYROMANCER_BOOTS.get());
//                            entries.accept(ItemRegistry.ELECTROMANCER_HELMET.get());
//                            entries.accept(ItemRegistry.ELECTROMANCER_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.ELECTROMANCER_LEGGINGS.get());
//                            entries.accept(ItemRegistry.ELECTROMANCER_BOOTS.get());
//                            entries.accept(ItemRegistry.ARCHEVOKER_HELMET.get());
//                            entries.accept(ItemRegistry.ARCHEVOKER_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.ARCHEVOKER_LEGGINGS.get());
//                            entries.accept(ItemRegistry.ARCHEVOKER_BOOTS.get());
//                            entries.accept(ItemRegistry.CULTIST_HELMET.get());
//                            entries.accept(ItemRegistry.CULTIST_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.CULTIST_LEGGINGS.get());
//                            entries.accept(ItemRegistry.CULTIST_BOOTS.get());
//                            entries.accept(ItemRegistry.CRYOMANCER_HELMET.get());
//                            entries.accept(ItemRegistry.CRYOMANCER_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.CRYOMANCER_LEGGINGS.get());
//                            entries.accept(ItemRegistry.CRYOMANCER_BOOTS.get());
//                            entries.accept(ItemRegistry.SHADOWWALKER_HELMET.get());
//                            entries.accept(ItemRegistry.SHADOWWALKER_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.SHADOWWALKER_LEGGINGS.get());
//                            entries.accept(ItemRegistry.SHADOWWALKER_BOOTS.get());
//                            entries.accept(ItemRegistry.PRIEST_HELMET.get());
//                            entries.accept(ItemRegistry.PRIEST_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.PRIEST_LEGGINGS.get());
//                            entries.accept(ItemRegistry.PRIEST_BOOTS.get());
//                            entries.accept(ItemRegistry.PLAGUED_HELMET.get());
//                            entries.accept(ItemRegistry.PLAGUED_CHESTPLATE.get());
//                            entries.accept(ItemRegistry.PLAGUED_LEGGINGS.get());
//                            entries.accept(ItemRegistry.PLAGUED_BOOTS.get());
//                            entries.accept(ItemRegistry.TARNISHED_CROWN.get());
//
//                            entries.accept(ItemRegistry.MANA_RING.get());
//                            entries.accept(ItemRegistry.SILVER_RING.get());
//                            entries.accept(ItemRegistry.COOLDOWN_RING.get());
//                            entries.accept(ItemRegistry.CAST_TIME_RING.get());
//                            entries.accept(ItemRegistry.HEAVY_CHAIN.get());
//                            entries.accept(ItemRegistry.EMERALD_STONEPLATE_RING.get());
//                            entries.accept(ItemRegistry.FIREWARD_RING.get());
//                            entries.accept(ItemRegistry.FROSTWARD_RING.get());
//                            entries.accept(ItemRegistry.POISONWARD_RING.get());
//                            entries.accept(ItemRegistry.CONJURERS_TALISMAN.get());
//                            entries.accept(ItemRegistry.AFFINITY_RING.get());
//
//                            entries.accept(ItemRegistry.WAYWARD_COMPASS.get());
//
//                        }));
    }
}
