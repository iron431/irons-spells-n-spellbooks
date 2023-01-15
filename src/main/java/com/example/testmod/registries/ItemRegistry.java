package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    //public static final RegistryObject<Item> SPELL_BOOK = ITEMS.register("spell_book", SpellBook::new);
    public static final RegistryObject<Item> WIMPY_SPELL_BOOK = ITEMS.register("wimpy_spell_book", SpellBook::new);
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", Scroll::new);
    public static final RegistryObject<Item> LIGHTNING_BOTTLE = ITEMS.register("lightning_bottle", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> FROZEN_BONE_SHARD = ITEMS.register("frozen_bone_shard", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> BLOOD_VIAL = ITEMS.register("blood_vial", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));

    public static final RegistryObject<Item> INSCRIPTION_TABLE_BLOCK_ITEM = ITEMS.register("inscription_table", () -> new BlockItem(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<Item> BLOOD_SLASH_BLOCK_ITEM = ITEMS.register("blood_slash_block", () -> new BlockItem(BlockRegistry.BLOOD_SLASH_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));
}
