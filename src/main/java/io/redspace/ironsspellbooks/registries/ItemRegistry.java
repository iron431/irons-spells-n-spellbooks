package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;

import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.spells.blood.BloodSlashSpell;
import io.redspace.ironsspellbooks.spells.blood.BloodStepSpell;
import io.redspace.ironsspellbooks.spells.blood.RayOfSiphoningSpell;
import io.redspace.ironsspellbooks.spells.blood.WitherSkullSpell;
import io.redspace.ironsspellbooks.spells.evocation.FangStrikeSpell;
import io.redspace.ironsspellbooks.spells.evocation.FangWardSpell;
import io.redspace.ironsspellbooks.spells.evocation.SummonVexSpell;
import io.redspace.ironsspellbooks.spells.fire.BlazeStormSpell;
import io.redspace.ironsspellbooks.item.armor.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IronsSpellbooks.MODID);

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
    public static final RegistryObject<Item> EVOKER_SPELL_BOOK = ITEMS.register("evoker_spell_book", () -> new UniqueSpellBook(SpellRarity.COMMON, new AbstractSpell[]{new FangStrikeSpell(6), new FangWardSpell(4), new SummonVexSpell(4)}));
    public static final RegistryObject<Item> BLOOD_STAFF = ITEMS.register("blood_staff", () -> new UniqueSpellBook(SpellRarity.COMMON, new AbstractSpell[]{new WitherSkullSpell(6), new RayOfSiphoningSpell(6), new BloodStepSpell(3), new BloodSlashSpell(6), new BlazeStormSpell(6)}));
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
    public static final RegistryObject<Item> MAGIC_CLOTH = ITEMS.register("magic_cloth", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_MATERIALS)));
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

    public static final RegistryObject<Item> WANDERING_MAGICIAN_HELMET = ITEMS.register("wandering_magician_helmet", () -> new WanderingMagicianArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> WANDERING_MAGICIAN_CHESTPLATE = ITEMS.register("wandering_magician_chestplate", () -> new WanderingMagicianArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> WANDERING_MAGICIAN_LEGGINGS = ITEMS.register("wandering_magician_leggings", () -> new WanderingMagicianArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> WANDERING_MAGICIAN_BOOTS = ITEMS.register("wandering_magician_boots", () -> new WanderingMagicianArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> PUMPKIN_HELMET = ITEMS.register("pumpkin_helmet", () -> new PumpkinArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PUMPKIN_CHESTPLATE = ITEMS.register("pumpkin_chestplate", () -> new PumpkinArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PUMPKIN_LEGGINGS = ITEMS.register("pumpkin_leggings", () -> new PumpkinArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PUMPKIN_BOOTS = ITEMS.register("pumpkin_boots", () -> new PumpkinArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> PYROMANCER_HELMET = ITEMS.register("pyromancer_helmet", () -> new PyromancerArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PYROMANCER_CHESTPLATE = ITEMS.register("pyromancer_chestplate", () -> new PyromancerArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PYROMANCER_LEGGINGS = ITEMS.register("pyromancer_leggings", () -> new PyromancerArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PYROMANCER_BOOTS = ITEMS.register("pyromancer_boots", () -> new PyromancerArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> ELECTROMANCER_HELMET = ITEMS.register("electromancer_helmet", () -> new ElectromancerArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ELECTROMANCER_CHESTPLATE = ITEMS.register("electromancer_chestplate", () -> new ElectromancerArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ELECTROMANCER_LEGGINGS = ITEMS.register("electromancer_leggings", () -> new ElectromancerArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ELECTROMANCER_BOOTS = ITEMS.register("electromancer_boots", () -> new ElectromancerArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> ARCHEVOKER_HELMET = ITEMS.register("archevoker_helmet", () -> new ArchevokerArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ARCHEVOKER_CHESTPLATE = ITEMS.register("archevoker_chestplate", () -> new ArchevokerArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ARCHEVOKER_LEGGINGS = ITEMS.register("archevoker_leggings", () -> new ArchevokerArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> ARCHEVOKER_BOOTS = ITEMS.register("archevoker_boots", () -> new ArchevokerArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> CULTIST_HELMET = ITEMS.register("cultist_helmet", () -> new CultistArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> CULTIST_CHESTPLATE = ITEMS.register("cultist_chestplate", () -> new CultistArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> CULTIST_LEGGINGS = ITEMS.register("cultist_leggings", () -> new CultistArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> CULTIST_BOOTS = ITEMS.register("cultist_boots", () -> new CultistArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> CRYOMANCER_HELMET = ITEMS.register("cryomancer_helmet", () -> new CryomancerArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> CRYOMANCER_CHESTPLATE = ITEMS.register("cryomancer_chestplate", () -> new CryomancerArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> CRYOMANCER_LEGGINGS = ITEMS.register("cryomancer_leggings", () -> new CryomancerArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> CRYOMANCER_BOOTS = ITEMS.register("cryomancer_boots", () -> new CryomancerArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> SHADOWWALKER_HELMET = ITEMS.register("shadowwalker_helmet", () -> new ShadowwalkerArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> SHADOWWALKER_CHESTPLATE = ITEMS.register("shadowwalker_chestplate", () -> new ShadowwalkerArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> SHADOWWALKER_LEGGINGS = ITEMS.register("shadowwalker_leggings", () -> new ShadowwalkerArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> SHADOWWALKER_BOOTS = ITEMS.register("shadowwalker_boots", () -> new ShadowwalkerArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> PRIEST_HELMET = ITEMS.register("priest_helmet", () -> new PriestArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PRIEST_CHESTPLATE = ITEMS.register("priest_chestplate", () -> new PriestArmorItem(EquipmentSlot.CHEST, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PRIEST_LEGGINGS = ITEMS.register("priest_leggings", () -> new PriestArmorItem(EquipmentSlot.LEGS, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    public static final RegistryObject<Item> PRIEST_BOOTS = ITEMS.register("priest_boots", () -> new PriestArmorItem(EquipmentSlot.FEET, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    public static final RegistryObject<Item> TARNISHED_CROWN = ITEMS.register("tarnished_helmet", () -> new TarnishedCrownArmorItem(EquipmentSlot.HEAD, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON)));

}
