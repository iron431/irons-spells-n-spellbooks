package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.item.InkItem;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.item.UniqueSpellBook;
import com.example.testmod.item.armor.ElectromancerArmorItem;
import com.example.testmod.item.armor.PyromancerArmorItem;
import com.example.testmod.item.armor.TarnishedCrownArmorItem;
import com.example.testmod.item.armor.WanderingMagicianArmorItem;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellRarity;
import com.example.testmod.spells.evocation.FangStrikeSpell;
import com.example.testmod.spells.evocation.FangWardSpell;
import com.example.testmod.spells.evocation.SummonVexSpell;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
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
    /**
     * Spell items
     */
    public static final RegistryObject<Item> WIMPY_SPELL_BOOK = ITEMS.register("wimpy_spell_book", SpellBook::new);
    public static final RegistryObject<Item> LEGENDARY_SPELL_BOOK = ITEMS.register("legendary_spell_book", () -> new SpellBook(12, SpellRarity.LEGENDARY));
    public static final RegistryObject<Item> NETHERITE_SPELL_BOOK = ITEMS.register("netherite_spell_book", () -> new SpellBook(9, SpellRarity.LEGENDARY));
    public static final RegistryObject<Item> DIAMOND_SPELL_BOOK = ITEMS.register("diamond_spell_book", () -> new SpellBook(7, SpellRarity.EPIC));
    public static final RegistryObject<Item> GOLD_SPELL_BOOK = ITEMS.register("gold_spell_book", () -> new SpellBook(5, SpellRarity.RARE));
    public static final RegistryObject<Item> IRON_SPELL_BOOK = ITEMS.register("iron_spell_book", () -> new SpellBook(4, SpellRarity.UNCOMMON));
    public static final RegistryObject<Item> COPPER_SPELL_BOOK = ITEMS.register("copper_spell_book", () -> new SpellBook(3, SpellRarity.COMMON));
    public static final RegistryObject<Item> EVOKER_SPELL_BOOK = ITEMS.register("evoker_spell_book", () -> new UniqueSpellBook(SpellRarity.COMMON, new AbstractSpell[]{new FangStrikeSpell(4), new FangWardSpell(3), new SummonVexSpell(3)}));
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", Scroll::new);

    /**
     * Ink
     */
    public static final RegistryObject<Item> INK_COMMON = ITEMS.register("common_ink", () -> new InkItem(SpellRarity.COMMON));
    public static final RegistryObject<Item> INK_UNCOMMON = ITEMS.register("uncommon_ink", () -> new InkItem(SpellRarity.UNCOMMON));
    public static final RegistryObject<Item> INK_RARE = ITEMS.register("rare_ink", () -> new InkItem(SpellRarity.RARE));
    public static final RegistryObject<Item> INK_EPIC = ITEMS.register("epic_ink", () -> new InkItem(SpellRarity.EPIC));
    public static final RegistryObject<Item> INK_LEGENDARY = ITEMS.register("legendary_ink", () -> new InkItem(SpellRarity.LEGENDARY));

    /**
     * Materials
     */
    public static final RegistryObject<Item> LIGHTNING_BOTTLE = ITEMS.register("lightning_bottle", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> FROZEN_BONE_SHARD = ITEMS.register("frozen_bone", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> BLOOD_VIAL = ITEMS.register("blood_vial", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> DIVINE_PEARL = ITEMS.register("divine_pearl", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> HOGSKIN = ITEMS.register("hogskin", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> MAGIC_DUST = ITEMS.register("magic_dust", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> CLOTH = ITEMS.register("cloth", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> BLANK_RUNE = ITEMS.register("blank_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> FIRE_RUNE = ITEMS.register("fire_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> ICE_RUNE = ITEMS.register("ice_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> LIGHTNING_RUNE = ITEMS.register("lightning_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> ENDER_RUNE = ITEMS.register("ender_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> HOLY_RUNE = ITEMS.register("holy_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> BLOOD_RUNE = ITEMS.register("blood_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    public static final RegistryObject<Item> EVOCATION_RUNE = ITEMS.register("evocation_rune", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    //public static final RegistryObject<Item> FIRE_CLOTH = ITEMS.register("fire_cloth", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    //public static final RegistryObject<Item> MAGIC_THREAD = ITEMS.register("magic_thread", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
    //public static final RegistryObject<Item> ENCHANTED_THREAD = ITEMS.register("enchanted_thread", () -> new SimpleFoiledItem((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RUINED_BOOK = ITEMS.register("ruined_book", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS).rarity(Rarity.EPIC)));

    /**
     * Block Items
     */
    public static final RegistryObject<Item> INSCRIPTION_TABLE_BLOCK_ITEM = ITEMS.register("inscription_table", () -> new BlockItem(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<Item> ACANE_ANVIL_BLOCK_ITEM = ITEMS.register("arcane_anvil", () -> new BlockItem(BlockRegistry.ARCANE_ANVIL_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<Item> SCROLL_FORGE_BLOCK = ITEMS.register("scroll_forge", () -> new BlockItem(BlockRegistry.SCROLL_FORGE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<Item> PEDESTAL_BLOCK_ITEM = ITEMS.register("pedestal", () -> new BlockItem(BlockRegistry.PEDESTAL_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<Item> BLOOD_SLASH_BLOCK_ITEM = ITEMS.register("blood_slash_block", () -> new BlockItem(BlockRegistry.BLOOD_SLASH_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    /**
     * Armor
     */
//    public static final RegistryObject<Item> WIZARD_HAT = ITEMS.register("wizard_hat", () -> new WizardArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
//    public static final RegistryObject<Item> WIZARD_ROBE = ITEMS.register("wizard_robe", () -> new WizardArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
//    public static final RegistryObject<Item> WIZARD_PANTS = ITEMS.register("wizard_pants", () -> new WizardArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
//    public static final RegistryObject<Item> WIZARD_BOOTS = ITEMS.register("wizard_boots", () -> new WizardArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> WANDERING_MAGICIAN_HAT = ITEMS.register("wandering_magician_helmet", () -> new WanderingMagicianArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> WANDERING_MAGICIAN_ROBE = ITEMS.register("wandering_magician_chestplate", () -> new WanderingMagicianArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> WANDERING_MAGICIAN_PANTS = ITEMS.register("wandering_magician_leggings", () -> new WanderingMagicianArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> WANDERING_MAGICIAN_BOOTS = ITEMS.register("wandering_magician_boots", () -> new WanderingMagicianArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> PYROMANCER_HAT = ITEMS.register("pyromancer_helmet", () -> new PyromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PYROMANCER_ROBE = ITEMS.register("pyromancer_chestplate", () -> new PyromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PYROMANCER_PANTS = ITEMS.register("pyromancer_leggings", () -> new PyromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PYROMANCER_BOOTS = ITEMS.register("pyromancer_boots", () -> new PyromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> ELECTROMANCER_HAT = ITEMS.register("electromancer_helmet", () -> new ElectromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ELECTROMANCER_ROBE = ITEMS.register("electromancer_chestplate", () -> new ElectromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ELECTROMANCER_PANTS = ITEMS.register("electromancer_leggings", () -> new ElectromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ELECTROMANCER_BOOTS = ITEMS.register("electromancer_boots", () -> new ElectromancerArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> TARNISHED_CROWN = ITEMS.register("tarnished_helmet", () -> new TarnishedCrownArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON)));

}
