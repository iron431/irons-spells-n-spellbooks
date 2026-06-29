package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.ArchevokerLogbookItem;
import io.redspace.ironsspellbooks.item.ChronicleItem;
import io.redspace.ironsspellbooks.item.CinderousSoulcallerItem;
import io.redspace.ironsspellbooks.item.CursedDollSpellbookItem;
import io.redspace.ironsspellbooks.item.DragonskinItem;
import io.redspace.ironsspellbooks.item.EldritchManuscript;
import io.redspace.ironsspellbooks.item.EnergizedCoreItem;
import io.redspace.ironsspellbooks.item.FurledMapCraftableItem;
import io.redspace.ironsspellbooks.item.FurledMapItem;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.item.NecronomiconSpellBook;
import io.redspace.ironsspellbooks.item.PortalFrameBlockItem;
import io.redspace.ironsspellbooks.item.RuinedBookItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.ShrivingStoneItem;
import io.redspace.ironsspellbooks.item.SimpleDescriptiveBlockItem;
import io.redspace.ironsspellbooks.item.SimpleDescriptiveItem;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.SpellSlotUpgradeItem;
import io.redspace.ironsspellbooks.item.UnchainedBookItem;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.item.WaywardCompass;
import io.redspace.ironsspellbooks.item.armor.ArchevokerArmorItem;
import io.redspace.ironsspellbooks.item.armor.BootsOfSpeedArmorItem;
import io.redspace.ironsspellbooks.item.armor.CryomancerArmorItem;
import io.redspace.ironsspellbooks.item.armor.CultistArmorItem;
import io.redspace.ironsspellbooks.item.armor.ElectromancerArmorItem;
import io.redspace.ironsspellbooks.item.armor.GoldCrownArmorItem;
import io.redspace.ironsspellbooks.item.armor.InfernalSorcererArmorItem;
import io.redspace.ironsspellbooks.item.armor.NetheriteMageArmorItem;
import io.redspace.ironsspellbooks.item.armor.PaladinArmorItem;
import io.redspace.ironsspellbooks.item.armor.PlaguedArmorItem;
import io.redspace.ironsspellbooks.item.armor.PriestArmorItem;
import io.redspace.ironsspellbooks.item.armor.PumpkinArmorItem;
import io.redspace.ironsspellbooks.item.armor.PyromancerArmorItem;
import io.redspace.ironsspellbooks.item.armor.ShadowwalkerArmorItem;
import io.redspace.ironsspellbooks.item.armor.TarnishedCrownArmorItem;
import io.redspace.ironsspellbooks.item.armor.WanderingMagicianArmorItem;
import io.redspace.ironsspellbooks.item.armor.WizardArmorItem;
import io.redspace.ironsspellbooks.item.consumables.FireAleItem;
import io.redspace.ironsspellbooks.item.consumables.NetherwardTinctureItem;
import io.redspace.ironsspellbooks.item.consumables.OakskinElixir;
import io.redspace.ironsspellbooks.item.consumables.SimpleElixir;
import io.redspace.ironsspellbooks.item.consumables.TinctureOfForgetfulnessItem;
import io.redspace.ironsspellbooks.item.curios.AffinityRing;
import io.redspace.ironsspellbooks.item.curios.BetrayerSignetRingItem;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.item.curios.ExpulsionRing;
import io.redspace.ironsspellbooks.item.curios.FirewardRing;
import io.redspace.ironsspellbooks.item.curios.FrostwardRing;
import io.redspace.ironsspellbooks.item.curios.InvisibiltyRing;
import io.redspace.ironsspellbooks.item.curios.LurkerRing;
import io.redspace.ironsspellbooks.item.curios.PoisonwardRing;
import io.redspace.ironsspellbooks.item.curios.SimpleDescriptiveCurio;
import io.redspace.ironsspellbooks.item.curios.TeleportationAmuletItem;
import io.redspace.ironsspellbooks.item.curios.VisibilityRing;
import io.redspace.ironsspellbooks.item.curios.WickedBoneRingItem;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.AutoloaderCrossbow;
import io.redspace.ironsspellbooks.item.weapons.ExtendedWeaponTier;
import io.redspace.ironsspellbooks.item.weapons.HitherThitherWand;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffOfTheNines;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import io.redspace.ironsspellbooks.item.weapons.TwilightGaleItem;
import io.redspace.ironsspellbooks.item.weapons.pyrium_staff.PyriumStaffItem;
import io.redspace.ironsspellbooks.render.CinderousRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiscFragmentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    /**
     * Spell items
     */
    public static final DeferredHolder<Item, Item> WIMPY_SPELL_BOOK = registerItem("wimpy_spell_book",
            (properties) -> new SpellBook(0, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())/*ItemPropertiesHelper.of().with(Item.Properties::stacksTo, 1).with(Item.Properties::rarity, Rarity.UNCOMMON).build()*/);
    public static final DeferredHolder<Item, Item> LEGENDARY_SPELL_BOOK = registerItem("legendary_spell_book",
            (properties) -> new SpellBook(12, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> NETHERITE_SPELL_BOOK = registerItem("netherite_spell_book",
            (properties) -> new SpellBook(12, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.COOLDOWN_REDUCTION, .20, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> DIAMOND_SPELL_BOOK = registerItem("diamond_spell_book",
            (properties) -> new SpellBook(10, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.MAX_MANA, 100, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> GOLD_SPELL_BOOK = registerItem("gold_spell_book",
            (properties) -> new SpellBook(8, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.CAST_TIME_REDUCTION, .15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 50, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> IRON_SPELL_BOOK = registerItem("iron_spell_book",
            (properties) -> new SpellBook(6, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> COPPER_SPELL_BOOK = registerItem("copper_spell_book",
            (properties) -> new SpellBook(5, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> ROTTEN_SPELL_BOOK = registerItem("rotten_spell_book",
            (properties) -> new SpellBook(8, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()).withSpellbookAttributes(new AttributeContainer(AttributeRegistry.SPELL_RESIST, -.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 100, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> BLAZE_SPELL_BOOK = registerItem("blaze_spell_book",
            (properties) -> new SpellBook(10, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.FIRE_SPELL_POWER, .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> DRAGONSKIN_SPELL_BOOK = registerItem("dragonskin_spell_book",
            (properties) -> new SpellBook(12, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.ENDER_SPELL_POWER, .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> DRUIDIC_SPELL_BOOK = registerItem("druidic_spell_book",
            (properties) -> new SpellBook(10, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.NATURE_SPELL_POWER, .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> VILLAGER_SPELL_BOOK = registerItem("villager_spell_book",
            (properties) -> new SpellBook(10, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.HOLY_SPELL_POWER, .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.CAST_TIME_REDUCTION, .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE)));
    public static final DeferredHolder<Item, Item> ICE_SPELL_BOOK = registerItem("ice_spell_book",
            (properties) -> new SpellBook(12, properties.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
                    .withSpellbookAttributes(new AttributeContainer(AttributeRegistry.ICE_SPELL_POWER, .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE)));

    public static final DeferredHolder<Item, Item> GRAYBEARD_STAFF = registerItem("graybeard_staff",
            (properties) -> new StaffItem(properties.stacksTo(1).fireResistant().attributes(ExtendedSwordItem.createAttributes(StaffTier.GRAYBEARD))));
    public static final DeferredHolder<Item, Item> PYRIUM_STAFF = registerItem("pyrium_staff",
            (properties) -> new PyriumStaffItem(properties.stacksTo(1).attributes(ExtendedSwordItem.createAttributes(StaffTier.PYRIUM_STAFF)).rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).fireResistant()));
    public static final DeferredHolder<Item, Item> ARTIFICER_STAFF = registerItem("artificer_cane",
            (properties) -> new StaffItem(properties.stacksTo(1).fireResistant().attributes(ExtendedSwordItem.createAttributes(StaffTier.ARTIFICER))));
    public static final DeferredHolder<Item, Item> ICE_STAFF = registerItem("ice_staff",
            (properties) -> new StaffItem(properties.stacksTo(1).fireResistant().attributes(ExtendedSwordItem.createAttributes(StaffTier.ICE_STAFF)).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> LIGHTNING_ROD_STAFF = registerItem("lightning_rod",
            (properties) -> new StaffItem(properties.stacksTo(1).fireResistant().attributes(ExtendedSwordItem.createAttributes(StaffTier.LIGHTNING_ROD)).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> BLOOD_STAFF = registerItem("blood_staff",
            (properties) -> new StaffItem(properties.stacksTo(1).fireResistant().attributes(ExtendedSwordItem.createAttributes(StaffTier.BLOOD_STAFF)).rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> EVOKER_SPELL_BOOK = registerItem("evoker_spell_book",
            (properties) -> new UniqueSpellBook(
                    new SpellDataRegistryHolder[]{
                            new SpellDataRegistryHolder(SpellRegistry.FANG_STRIKE_SPELL, 6),
                            new SpellDataRegistryHolder(SpellRegistry.FANG_WARD_SPELL, 4),
                            new SpellDataRegistryHolder(SpellRegistry.SUMMON_VEX_SPELL, 4)},
                    7, properties.fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)).withSpellbookAttributes(new AttributeContainer(AttributeRegistry.EVOCATION_SPELL_POWER, .10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MAX_MANA, 200, AttributeModifier.Operation.ADD_VALUE))
    );
    public static final DeferredHolder<Item, Item> NECRONOMICON = registerItem("necronomicon_spell_book",
            (properties) -> new NecronomiconSpellBook(properties.stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> CURSED_DOLL_SPELLBOOK = registerItem("cursed_doll_spell_book",
            (properties) -> new CursedDollSpellbookItem(properties.stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> MAGEHUNTER = registerItem("magehunter",
            (properties) -> new ExtendedSwordItem(ExtendedWeaponTier.METAL_MAGEHUNTER, properties.fireResistant().attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.METAL_MAGEHUNTER))));
    public static final DeferredHolder<Item, Item> SPELLBREAKER = registerItem("spellbreaker",
            (properties) -> new MagicSwordItem(ExtendedWeaponTier.SPELLBREAKER, properties.rarity(Rarity.EPIC).fireResistant().attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.SPELLBREAKER)), SpellDataRegistryHolder.of(new SpellDataRegistryHolder(SpellRegistry.COUNTERSPELL_SPELL, 1))));
    public static final DeferredHolder<Item, Item> TEST_CLAYMORE = registerItem("claymore",
            (properties) -> new ExtendedSwordItem(ExtendedWeaponTier.CLAYMORE, properties.attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.CLAYMORE))));
    public static final DeferredHolder<Item, Item> KEEPER_FLAMBERGE = registerItem("keeper_flamberge",
            (properties) -> new ExtendedSwordItem(ExtendedWeaponTier.DECREPIT_FLAMBERGE, properties.rarity(Rarity.UNCOMMON).fireResistant().attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.DECREPIT_FLAMBERGE))));
    public static final DeferredHolder<Item, Item> LEGIONNAIRE_FLAMBERGE = registerItem("legionnaire_flamberge",
            (properties) -> new ExtendedSwordItem(ExtendedWeaponTier.LEGIONNAIRE_FLAMBERGE, properties.rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).fireResistant().attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.LEGIONNAIRE_FLAMBERGE))));
    public static final DeferredHolder<Item, Item> AMETHYST_RAPIER = registerItem("amethyst_rapier",
            (properties) -> new MagicSwordItem(ExtendedWeaponTier.AMETHYST_RAPIER, properties.rarity(Rarity.EPIC).fireResistant().attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.AMETHYST_RAPIER)), SpellDataRegistryHolder.of(new SpellDataRegistryHolder(SpellRegistry.ECHOING_STRIKES_SPELL, 5))));
    public static final DeferredHolder<Item, Item> MISERY = registerItem("misery",
            (properties) -> new MagicSwordItem(ExtendedWeaponTier.MISERY, properties.attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.MISERY)), SpellDataRegistryHolder.of(new SpellDataRegistryHolder(SpellRegistry.WITHER_SKULL_SPELL, 8))));
    public static final DeferredHolder<Item, Item> SCROLL = registerItem("scroll",
            (properties) -> new Scroll(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> AUTOLOADER_CROSSBOW = registerItem("autoloader_crossbow",
            (properties) -> new AutoloaderCrossbow(properties.stacksTo(1).durability(465)));
    public static final DeferredHolder<Item, Item> HITHER_THITHER_WAND = registerItem("hither_thither_wand",
            (properties) -> new HitherThitherWand(properties.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> STAFF_OF_THE_NINES = registerItem("staff_of_the_nines",
            (properties) -> new StaffOfTheNines(properties.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> HELLRAZOR = registerItem("hellrazor",
            (properties) -> new MagicSwordItem(ExtendedWeaponTier.HELLRAZOR, properties.attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.HELLRAZOR)).rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).fireResistant(), SpellDataRegistryHolder.of(new SpellDataRegistryHolder(SpellRegistry.RAISE_HELL_SPELL, 3))));
    public static final DeferredHolder<Item, Item> DECREPIT_SCYTHE = registerItem("decrepit_scythe",
            (properties) -> new ExtendedSwordItem(ExtendedWeaponTier.DECREPIT_SCYTHE, properties.attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.DECREPIT_SCYTHE)).rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> ICE_GREATSWORD = registerItem("boreal_blade",
            (properties) -> new MagicSwordItem(ExtendedWeaponTier.ICE_GREATSWORD,
                    properties.attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.ICE_GREATSWORD))
                            .rarity(Rarity.RARE)
                            .fireResistant(), SpellDataRegistryHolder.of(new SpellDataRegistryHolder(SpellRegistry.FROSTBITE_SPELL, 3))));
    public static final DeferredHolder<Item, Item> TWILIGHT_GALE = registerItem("twilight_gale",
            (properties) -> new TwilightGaleItem(ExtendedWeaponTier.TWILIGHT_GALE,
                    properties.attributes(ExtendedSwordItem.createAttributes(ExtendedWeaponTier.TWILIGHT_GALE))
                            .rarity(Rarity.RARE)
                            .fireResistant(), SpellDataRegistryHolder.of(new SpellDataRegistryHolder(SpellRegistry.VOLT_STRIKE_SPELL, 5))));

    /**
     * Ink
     */
    public static final DeferredHolder<Item, Item> INK_COMMON = registerItem("common_ink",
            (properties) -> new InkItem(SpellRarity.COMMON, FluidRegistry.COMMON_INK, properties));
    public static final DeferredHolder<Item, Item> INK_UNCOMMON = registerItem("uncommon_ink",
            (properties) -> new InkItem(SpellRarity.UNCOMMON, FluidRegistry.UNCOMMON_INK, properties));
    public static final DeferredHolder<Item, Item> INK_RARE = registerItem("rare_ink",
            (properties) -> new InkItem(SpellRarity.RARE, FluidRegistry.RARE_INK, properties));
    public static final DeferredHolder<Item, Item> INK_EPIC = registerItem("epic_ink",
            (properties) -> new InkItem(SpellRarity.EPIC, FluidRegistry.EPIC_INK, properties));
    public static final DeferredHolder<Item, Item> INK_LEGENDARY = registerItem("legendary_ink",
            (properties) -> new InkItem(SpellRarity.LEGENDARY, FluidRegistry.LEGENDARY_INK, properties));

    /**
     * Potions
     */
    public static final DeferredHolder<Item, Item> OAKSKIN_ELIXIR = registerItem("oakskin_elixir",
            (properties) -> new OakskinElixir(properties.stacksTo(4),
                    () -> new MobEffectInstance(MobEffectRegistry.OAKSKIN, 1200, 2)));
    public static final DeferredHolder<Item, Item> GREATER_OAKSKIN_ELIXIR = registerItem("greater_oakskin_elixir",
            (properties) -> new OakskinElixir(properties.stacksTo(4),
                    () -> new MobEffectInstance(MobEffectRegistry.OAKSKIN, 2400, 6), true));
    public static final DeferredHolder<Item, Item> GREATER_HEALING_POTION = registerItem("greater_healing_potion",
            (properties) -> new SimpleElixir(properties.stacksTo(4),
                    () -> new MobEffectInstance(MobEffects.HEAL, 1, 2)));
    public static final DeferredHolder<Item, Item> INVISIBILITY_ELIXIR = registerItem("invisibility_elixir",
            (properties) -> new SimpleElixir(properties.stacksTo(4),
                    () -> new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY, 20 * 15, 0, false, false, true)));
    public static final DeferredHolder<Item, Item> GREATER_INVISIBILITY_ELIXIR = registerItem("greater_invisibility_elixir",
            (properties) -> new SimpleElixir(properties.stacksTo(4),
                    () -> new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY, 20 * 40, 0, false, false, true), true));
    public static final DeferredHolder<Item, Item> EVASION_ELIXIR = registerItem("evasion_elixir",
            (properties) -> new SimpleElixir(properties.stacksTo(4),
                    () -> new MobEffectInstance(MobEffectRegistry.EVASION, 20 * 60, 1, false, false, true)));
    public static final DeferredHolder<Item, Item> GREATER_EVASION_ELIXIR = registerItem("greater_evasion_elixir",
            (properties) -> new SimpleElixir(properties.stacksTo(4), () -> new MobEffectInstance(MobEffectRegistry.EVASION, 20 * 60, 3, false, false, true), true));
    public static final DeferredHolder<Item, Item> FIRE_ALE = registerItem("fire_ale",
            (properties) -> new FireAleItem(properties.stacksTo(4).fireResistant()));
    public static final DeferredHolder<Item, Item> NETHERWARD_TINCTURE = registerItem("netherward_tincture",
            (properties) -> new NetherwardTinctureItem(properties.stacksTo(16)));
    public static final DeferredHolder<Item, Item> TINCTURE_OF_FORGETFULNESS = registerItem("tincture_of_forgetfulness",
            (properties) -> new TinctureOfForgetfulnessItem(properties.stacksTo(16)));

    /**
     * Upgrade Orbs
     */
    public static final DeferredHolder<Item, Item> UPGRADE_ORB = registerItem("upgrade_orb",
            (properties) -> new Item(properties.rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> FIRE_UPGRADE_ORB = registerItem("fire_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.FIRE_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> ICE_UPGRADE_ORB = registerItem("ice_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.ICE_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> LIGHTNING_UPGRADE_ORB = registerItem("lightning_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.LIGHTNING_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> HOLY_UPGRADE_ORB = registerItem("holy_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.HOLY_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> ENDER_UPGRADE_ORB = registerItem("ender_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.ENDER_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> BLOOD_UPGRADE_ORB = registerItem("blood_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.BLOOD_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> EVOCATION_UPGRADE_ORB = registerItem("evocation_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.EVOCATION_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> NATURE_UPGRADE_ORB = registerItem("nature_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.NATURE_SPELL_POWER)));
    public static final DeferredHolder<Item, Item> MANA_UPGRADE_ORB = registerItem("mana_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.MANA)));
    public static final DeferredHolder<Item, Item> COOLDOWN_UPGRADE_ORB = registerItem("cooldown_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.COOLDOWN)));
    public static final DeferredHolder<Item, Item> PROTECTION_UPGRADE_ORB = registerItem("protection_upgrade_orb",
            (properties) -> new UpgradeOrbItem(properties.rarity(Rarity.UNCOMMON).fireResistant().component(ComponentRegistry.UPGRADE_ORB_TYPE, UpgradeOrbTypeRegistry.SPELL_RESISTANCE)));

    /**
     * Generic Items
     */
    public static final DeferredHolder<Item, Item> LIGHTNING_BOTTLE = registerItem("lightning_bottle",
            (properties) -> new Item(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> FROZEN_BONE_SHARD = registerItem("frozen_bone", Item::new);
    public static final DeferredHolder<Item, Item> BLOOD_VIAL = registerItem("blood_vial", Item::new);
    public static final DeferredHolder<Item, Item> ICE_VENOM_VIAL = registerItem("ice_venom_vial", Item::new);
    public static final DeferredHolder<Item, Item> DIVINE_PEARL = registerItem("divine_pearl", Item::new);
    public static final DeferredHolder<Item, Item> FURLED_MAP = registerItem("furled_map",
            (properties) -> new FurledMapItem(properties.stacksTo(1)));
    public static final DeferredHolder<Item, Item> ANCIENT_FURLED_MAP = registerItem("furled_map_ancient",
            (properties) -> new FurledMapItem(properties.stacksTo(1)));
    public static final DeferredHolder<Item, Item> CITADEL_FURLED_MAP = registerItem("furled_map_citadel",
            (properties) -> new FurledMapCraftableItem(true, new FurledMapItem.FurledMapData(IronsSpellbooks.id("citadel"), Optional.of(FurledMapItem.NETHER),
                    Optional.of(Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", Component.translatable("item.irons_spellbooks.citadel_map")).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)))), properties.stacksTo(1)));
    public static final DeferredHolder<Item, Item> ICE_SPIDER_FURLED_MAP = registerItem("furled_map_ice_spider_den",
            (properties) -> new FurledMapCraftableItem(false, new FurledMapItem.FurledMapData(IronsSpellbooks.id("ice_spider_den"), Optional.of(FurledMapItem.OVERWORLD),
                    Optional.of(Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", Component.translatable("item.irons_spellbooks.ice_spider_den_map")).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)))), properties.stacksTo(1)));
    public static final DeferredHolder<Item, Item> HOGSKIN = registerItem("hogskin", Item::new);
    public static final DeferredHolder<Item, Item> DRAGONSKIN = registerItem("dragonskin", DragonskinItem::new);
    public static final DeferredHolder<Item, Item> ARCANE_ESSENCE = registerItem("arcane_essence", Item::new);
    public static final DeferredHolder<Item, Item> MAGIC_CLOTH = registerItem("magic_cloth", Item::new);
    public static final DeferredHolder<Item, Item> BLANK_RUNE = registerItem("blank_rune", Item::new);
    public static final DeferredHolder<Item, Item> FIRE_RUNE = registerItem("fire_rune", Item::new);
    public static final DeferredHolder<Item, Item> ICE_RUNE = registerItem("ice_rune", Item::new);
    public static final DeferredHolder<Item, Item> LIGHTNING_RUNE = registerItem("lightning_rune", Item::new);
    public static final DeferredHolder<Item, Item> ENDER_RUNE = registerItem("ender_rune", Item::new);
    public static final DeferredHolder<Item, Item> HOLY_RUNE = registerItem("holy_rune", Item::new);
    public static final DeferredHolder<Item, Item> BLOOD_RUNE = registerItem("blood_rune", Item::new);
    public static final DeferredHolder<Item, Item> EVOCATION_RUNE = registerItem("evocation_rune", Item::new);
    public static final DeferredHolder<Item, Item> MANA_RUNE = registerItem("arcane_rune", Item::new);
    public static final DeferredHolder<Item, Item> COOLDOWN_RUNE = registerItem("cooldown_rune", Item::new);
    public static final DeferredHolder<Item, Item> PROTECTION_RUNE = registerItem("protection_rune", Item::new);
    public static final DeferredHolder<Item, Item> NATURE_RUNE = registerItem("nature_rune", Item::new);
    public static final DeferredHolder<Item, Item> WAYWARD_COMPASS = registerItem("wayward_compass", WaywardCompass::new);
    public static final DeferredHolder<Item, Item> RUINED_BOOK = registerItem("ruined_book",
            (properties) -> new RuinedBookItem(properties.rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> CINDER_ESSENCE = registerItem("cinder_essence", Item::new);
    public static final DeferredHolder<Item, Item> ARCANE_INGOT = registerItem("arcane_ingot", Item::new);
    public static final DeferredHolder<Item, Item> SHRIVING_STONE = registerItem("shriving_stone", ShrivingStoneItem::new);
    public static final DeferredHolder<Item, Item> LESSER_SPELL_SLOT_UPGRADE = registerItem("lesser_spell_slot_upgrade",
            (properties) -> new SpellSlotUpgradeItem(12, properties.rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ELDRITCH_PAGE = registerItem("eldritch_manuscript",
            (properties) -> new EldritchManuscript(properties.rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> LOST_KNOWLEDGE_FRAGMENT = registerItem("ancient_knowledge_fragment",
            (properties) -> new Item(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> FROSTED_HELVE = registerItem("frosted_helve",
            (properties) -> new Item(properties.rarity(Rarity.COMMON)));
    public static final DeferredHolder<Item, Item> ICE_CRYSTAL = registerItem("permafrost_shard",
            (properties) -> new Item(properties.rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> ENERGIZED_CORE = registerItem("energized_core",
            (properties) -> new EnergizedCoreItem(properties.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> CHAINED_BOOK = registerItem("chained_book", Item::new);
    public static final DeferredHolder<Item, Item> BLOODY_VELLUM = registerItem("bloody_vellum", Item::new);
    public static final DeferredHolder<Item, Item> ICY_FANG = registerItem("icy_fang", Item::new);
    public static final DeferredHolder<Item, Item> UNCHAINED_BOOK = registerItem("unchained_book",
            (properties) -> new UnchainedBookItem(properties.stacksTo(1).rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).component(DataComponents.WRITTEN_BOOK_CONTENT, UnchainedBookItem.CONTENTS)));

    public static final DeferredHolder<Item, Item> TIMELESS_SLURRY = registerItem("timeless_slurry",
            (properties) -> new Item(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> PYRIUM_INGOT = registerItem("pyrium_ingot",
            (properties) -> new Item(properties.rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).fireResistant()));
    public static final DeferredHolder<Item, Item> RAW_MITHRIL = registerItem("raw_mithril",
            (properties) -> new Item(properties.rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, Item> MITHRIL_SCRAP = registerItem("mithril_scrap",
            (properties) -> new Item(properties.rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, Item> MITHRIL_INGOT = registerItem("mithril_ingot",
            (properties) -> new Item(properties.rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, Item> MITHRIL_WEAVE = registerItem("mithril_weave",
            (properties) -> new Item(properties.rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, Item> WEAPON_PARTS = registerItem("weapon_parts",
            (properties) -> new Item(properties.rarity(Rarity.RARE).fireResistant()));
    public static final DeferredHolder<Item, Item> DIVINE_SOULSHARD = registerItem("divine_soulshard",
            (properties) -> new Item(properties.rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).fireResistant()));

    public static final DeferredHolder<Item, Item> TRANSLATED_ARCHEVOKER_LOGBOOK = registerItem("archevoker_logbook_translated",
            (properties) -> new ArchevokerLogbookItem(true, new Item.Properties().component(DataComponents.WRITTEN_BOOK_CONTENT, ArchevokerLogbookItem.TRANSLATED_CONTENTS)));
    public static final DeferredHolder<Item, Item> UNTRANSLATED_ARCHEVOKER_LOGBOOK = registerItem("archevoker_logbook_untranslated",
            (properties) -> new ArchevokerLogbookItem(false, new Item.Properties().component(DataComponents.WRITTEN_BOOK_CONTENT, ArchevokerLogbookItem.UNTRANSLATED_CONTENTS)));
    public static final DeferredHolder<Item, ChronicleItem> THE_CHRONICLE = registerItem("chronicle",
            (properties) -> new ChronicleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredHolder<Item, Item> CINDEROUS_SOULCALLER = registerItem("cinderous_soulcaller",
            (properties) -> new CinderousSoulcallerItem(properties.stacksTo(1).rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).fireResistant()));
    public static final DeferredHolder<Item, Item> DECREPIT_KEY = registerItem("decrepit_key",
            (properties) -> new SimpleDescriptiveItem(properties.rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> BONE_KEY = registerItem("bone_key",
            (properties) -> new SimpleDescriptiveItem(properties.rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> DEAD_KING_PHYLACTERY = registerItem("dead_king_phylactery",
            (properties) -> new SimpleDescriptiveItem(properties.rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredHolder<Item, Item> DEAD_KING_PHYLACTERY_SHARD = registerItem("dead_king_phylactery_shard",
            (properties) -> new Item(properties.rarity(Rarity.UNCOMMON).fireResistant()));


    /**
     * Block Items
     */
    public static final DeferredHolder<Item, Item> INSCRIPTION_TABLE_BLOCK_ITEM = registerItem("inscription_table",
            (properties) -> new BlockItem(BlockRegistry.INSCRIPTION_TABLE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ACANE_ANVIL_BLOCK_ITEM = registerItem("arcane_anvil",
            (properties) -> new BlockItem(BlockRegistry.ARCANE_ANVIL_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> SCROLL_FORGE_BLOCK = registerItem("scroll_forge",
            (properties) -> new BlockItem(BlockRegistry.SCROLL_FORGE_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> PEDESTAL_BLOCK_ITEM = registerItem("pedestal",
            (properties) -> new BlockItem(BlockRegistry.PEDESTAL_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ARMOR_PILE_BLOCK_ITEM = registerItem("armor_pile",
            (properties) -> new BlockItem(BlockRegistry.ARMOR_PILE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MITHRIL_ORE_BLOCK_ITEM = registerItem("mithril_ore",
            (properties) -> new BlockItem(BlockRegistry.MITHRIL_ORE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MITHRIL_ORE_DEEPSLATE_BLOCK_ITEM = registerItem("deepslate_mithril_ore",
            (properties) -> new BlockItem(BlockRegistry.MITHRIL_ORE_DEEPSLATE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ALCHEMIST_CAULDRON_BLOCK_ITEM = registerItem("alchemist_cauldron",
            (properties) -> new BlockItem(BlockRegistry.ALCHEMIST_CAULDRON.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> FIREFLY_JAR_ITEM = registerItem("firefly_jar",
            (properties) -> new BlockItem(BlockRegistry.FIREFLY_JAR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> PORTAL_FRAME_ITEM = registerItem("portal_frame",
            (properties) -> new PortalFrameBlockItem(new Item.Properties().fireResistant().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> BRAZIER_ITEM = registerItem("brazier",
            (properties) -> new BlockItem(BlockRegistry.BRAZIER_FIRE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SOUL_BRAZIER_ITEM = registerItem("brazier_soul",
            (properties) -> new BlockItem(BlockRegistry.BRAZIER_SOUL.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CINDEROUS_KEYSTONE_BLOCK_ITEM = registerItem("cinderous_soul_rune",
            (properties) -> new BlockItem(BlockRegistry.CINDEROUS_KEYSTONE.get(), new Item.Properties().rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue())));
    public static final DeferredHolder<Item, Item> ICE_SPIDER_EGG_BLOCK_ITEM = registerItem("ice_spider_egg",
            (properties) -> new BlockItem(BlockRegistry.ICE_SPIDER_EGG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> BOOK_STACK_BLOCK_ITEM = registerItem("book_stack",
            (properties) -> new SimpleDescriptiveBlockItem(BlockRegistry.BOOK_STACK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> WISEWOOD_BOOKSHELF_BLOCK_ITEM = registerItem("wisewood_bookshelf",
            (properties) -> new BlockItem(BlockRegistry.WISEWOOD_BOOKSHELF.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> WISEWOOD_CHISELED_BOOKSHELF_BLOCK_ITEM = registerItem("wisewood_chiseled_bookshelf",
            (properties) -> new BlockItem(BlockRegistry.WISEWOOD_CHISELLED_BOOKSHELF.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> NETHER_BRICK_PILLAR_BLOCK_ITEM = registerItem("nether_brick_pillar",
            (properties) -> new BlockItem(BlockRegistry.NETHER_BRICK_PILLAR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> TYROS_STATUE_BLOCK_ITEM = registerItem("tyros_statue",
            (properties) -> new BlockItem(BlockRegistry.TYROS_STATUE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BONE_VAULT_BLOCK_ITEM = registerItem("bone_vault",
            (properties) -> new BlockItem(BlockRegistry.BONE_VAULT_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CINDEROUS_VAULT_BLOCK_ITEM = registerItem("cinderous_vault",
            (properties) -> new BlockItem(BlockRegistry.CINDEROUS_VAULT_BLOCK.get(), new Item.Properties().rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue())));

    /**
     * Armor
     */
    public static final DeferredHolder<Item, Item> WANDERING_MAGICIAN_HELMET = registerItem("wandering_magician_helmet",
            (properties) -> new WanderingMagicianArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(22))));
    public static final DeferredHolder<Item, Item> WANDERING_MAGICIAN_CHESTPLATE = registerItem("wandering_magician_chestplate",
            (properties) -> new WanderingMagicianArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(22))));
    public static final DeferredHolder<Item, Item> WANDERING_MAGICIAN_LEGGINGS = registerItem("wandering_magician_leggings",
            (properties) -> new WanderingMagicianArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(22))));
    public static final DeferredHolder<Item, Item> WANDERING_MAGICIAN_BOOTS = registerItem("wandering_magician_boots",
            (properties) -> new WanderingMagicianArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(22))));

    public static final DeferredHolder<Item, Item> PUMPKIN_HELMET = registerItem("pumpkin_helmet",
            (properties) -> new PumpkinArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(26))));
    public static final DeferredHolder<Item, Item> PUMPKIN_CHESTPLATE = registerItem("pumpkin_chestplate",
            (properties) -> new PumpkinArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(26))));
    public static final DeferredHolder<Item, Item> PUMPKIN_LEGGINGS = registerItem("pumpkin_leggings",
            (properties) -> new PumpkinArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(26))));
    public static final DeferredHolder<Item, Item> PUMPKIN_BOOTS = registerItem("pumpkin_boots",
            (properties) -> new PumpkinArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(26))));

    public static final DeferredHolder<Item, Item> PYROMANCER_HELMET = registerItem("pyromancer_helmet",
            (properties) -> new PyromancerArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> PYROMANCER_CHESTPLATE = registerItem("pyromancer_chestplate",
            (properties) -> new PyromancerArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> PYROMANCER_LEGGINGS = registerItem("pyromancer_leggings",
            (properties) -> new PyromancerArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> PYROMANCER_BOOTS = registerItem("pyromancer_boots",
            (properties) -> new PyromancerArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> ELECTROMANCER_HELMET = registerItem("electromancer_helmet",
            (properties) -> new ElectromancerArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> ELECTROMANCER_CHESTPLATE = registerItem("electromancer_chestplate",
            (properties) -> new ElectromancerArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> ELECTROMANCER_LEGGINGS = registerItem("electromancer_leggings",
            (properties) -> new ElectromancerArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> ELECTROMANCER_BOOTS = registerItem("electromancer_boots",
            (properties) -> new ElectromancerArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> ARCHEVOKER_HELMET = registerItem("archevoker_helmet",
            (properties) -> new ArchevokerArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> ARCHEVOKER_CHESTPLATE = registerItem("archevoker_chestplate",
            (properties) -> new ArchevokerArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> ARCHEVOKER_LEGGINGS = registerItem("archevoker_leggings",
            (properties) -> new ArchevokerArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> ARCHEVOKER_BOOTS = registerItem("archevoker_boots",
            (properties) -> new ArchevokerArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> CULTIST_HELMET = registerItem("cultist_helmet",
            (properties) -> new CultistArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> CULTIST_CHESTPLATE = registerItem("cultist_chestplate",
            (properties) -> new CultistArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> CULTIST_LEGGINGS = registerItem("cultist_leggings",
            (properties) -> new CultistArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> CULTIST_BOOTS = registerItem("cultist_boots",
            (properties) -> new CultistArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> CRYOMANCER_HELMET = registerItem("cryomancer_helmet",
            (properties) -> new CryomancerArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> CRYOMANCER_CHESTPLATE = registerItem("cryomancer_chestplate",
            (properties) -> new CryomancerArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> CRYOMANCER_LEGGINGS = registerItem("cryomancer_leggings",
            (properties) -> new CryomancerArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> CRYOMANCER_BOOTS = registerItem("cryomancer_boots",
            (properties) -> new CryomancerArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> SHADOWWALKER_HELMET = registerItem("shadowwalker_helmet",
            (properties) -> new ShadowwalkerArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> SHADOWWALKER_CHESTPLATE = registerItem("shadowwalker_chestplate",
            (properties) -> new ShadowwalkerArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> SHADOWWALKER_LEGGINGS = registerItem("shadowwalker_leggings",
            (properties) -> new ShadowwalkerArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> SHADOWWALKER_BOOTS = registerItem("shadowwalker_boots",
            (properties) -> new ShadowwalkerArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> PRIEST_HELMET = registerItem("priest_helmet",
            (properties) -> new PriestArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> PRIEST_CHESTPLATE = registerItem("priest_chestplate",
            (properties) -> new PriestArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> PRIEST_LEGGINGS = registerItem("priest_leggings",
            (properties) -> new PriestArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> PRIEST_BOOTS = registerItem("priest_boots",
            (properties) -> new PriestArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> PLAGUED_HELMET = registerItem("plagued_helmet",
            (properties) -> new PlaguedArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> PLAGUED_CHESTPLATE = registerItem("plagued_chestplate",
            (properties) -> new PlaguedArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> PLAGUED_LEGGINGS = registerItem("plagued_leggings",
            (properties) -> new PlaguedArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> PLAGUED_BOOTS = registerItem("plagued_boots",
            (properties) -> new PlaguedArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> NETHERITE_MAGE_HELMET = registerItem("netherite_mage_helmet",
            (properties) -> new NetheriteMageArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).fireResistant().durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> NETHERITE_MAGE_CHESTPLATE = registerItem("netherite_mage_chestplate",
            (properties) -> new NetheriteMageArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> NETHERITE_MAGE_LEGGINGS = registerItem("netherite_mage_leggings",
            (properties) -> new NetheriteMageArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> NETHERITE_MAGE_BOOTS = registerItem("netherite_mage_boots",
            (properties) -> new NetheriteMageArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> WIZARD_HELMET = registerItem("wizard_helmet",
            (properties) -> new WizardArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37))));
    public static final DeferredHolder<Item, Item> WIZARD_HAT = registerItem("wizard_hat",
            (properties) -> new WizardArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).durability(ArmorItem.Type.HELMET.getDurability(37)).component(ComponentRegistry.CLOTHING_VARIANT, "hat")));
    public static final DeferredHolder<Item, Item> WIZARD_CHESTPLATE = registerItem("wizard_chestplate",
            (properties) -> new WizardArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> WIZARD_LEGGINGS = registerItem("wizard_leggings",
            (properties) -> new WizardArmorItem(ArmorItem.Type.LEGGINGS, properties.stacksTo(1).durability(ArmorItem.Type.LEGGINGS.getDurability(37))));
    public static final DeferredHolder<Item, Item> WIZARD_BOOTS = registerItem("wizard_boots",
            (properties) -> new WizardArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).durability(ArmorItem.Type.BOOTS.getDurability(37))));

    public static final DeferredHolder<Item, Item> PALADIN_CHESTPLATE = registerItem("paladin_chestplate",
            (properties) -> new PaladinArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));
    public static final DeferredHolder<Item, Item> BOOTS_OF_SPEED = registerItem("speed_boots",
            (properties) -> new BootsOfSpeedArmorItem(ArmorItem.Type.BOOTS, properties.stacksTo(1).rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))));
    public static final DeferredHolder<Item, Item> INFERNAL_SORCERER_CHESTPLATE = registerItem("infernal_sorcerer_chestplate",
            (properties) -> new InfernalSorcererArmorItem(ArmorItem.Type.CHESTPLATE, properties.stacksTo(1).rarity(Rarity.EPIC).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))));

    public static final DeferredHolder<Item, Item> TARNISHED_CROWN = registerItem("tarnished_helmet",
            (properties) -> new TarnishedCrownArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).rarity(Rarity.UNCOMMON).durability(ArmorItem.Type.HELMET.getDurability(26))));
    public static final DeferredHolder<Item, Item> DEV_CROWN = registerItem("gold_crown",
            (properties) -> new GoldCrownArmorItem(ArmorItem.Type.HELMET, properties.stacksTo(1).rarity(Rarity.EPIC)));

    /**
     * Curios
     */
    public static final Supplier<CurioBaseItem> MANA_RING = registerItem("mana_ring",
            (properties) -> new CurioBaseItem(properties.stacksTo(1)).withAttributes(Curios.RING_SLOT, new AttributeContainer(AttributeRegistry.MAX_MANA, 100, AttributeModifier.Operation.ADD_VALUE)));
    public static final Supplier<CurioBaseItem> SILVER_RING = registerItem("silver_ring",
            (properties) -> new CurioBaseItem(properties.stacksTo(1)).withAttributes(Curios.RING_SLOT, new AttributeContainer(AttributeRegistry.MAX_MANA, 25, AttributeModifier.Operation.ADD_VALUE)));
    public static final Supplier<CurioBaseItem> COOLDOWN_RING = registerItem("cooldown_ring",
            (properties) -> new CurioBaseItem(properties.stacksTo(1)).withAttributes(Curios.RING_SLOT, new AttributeContainer(AttributeRegistry.COOLDOWN_REDUCTION, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    public static final Supplier<CurioBaseItem> CAST_TIME_RING = registerItem("cast_time_ring",
            (properties) -> new CurioBaseItem(properties.stacksTo(1)).withAttributes(Curios.RING_SLOT, new AttributeContainer(AttributeRegistry.CAST_TIME_REDUCTION, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    public static final Supplier<CurioBaseItem> HEAVY_CHAIN = registerItem("heavy_chain_necklace",
            (properties) -> new CurioBaseItem(properties.stacksTo(1)).withAttributes(Curios.NECKLACE_SLOT, new AttributeContainer(AttributeRegistry.SPELL_RESIST, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    public static final Supplier<CurioBaseItem> EMERALD_STONEPLATE_RING = registerItem("emerald_stoneplate_ring",
            (properties) -> new SimpleDescriptiveCurio(properties.stacksTo(1), Curios.RING_SLOT));
    public static final Supplier<CurioBaseItem> FIREWARD_RING = registerItem("fireward_ring",
            (properties) -> new FirewardRing(properties.stacksTo(1)));
    public static final Supplier<CurioBaseItem> FROSTWARD_RING = registerItem("frostward_ring",
            (properties) -> new FrostwardRing(properties.stacksTo(1)));
    public static final Supplier<CurioBaseItem> POISONWARD_RING = registerItem("poisonward_ring",
            (properties) -> new PoisonwardRing(properties.stacksTo(1)));
    public static final Supplier<CurioBaseItem> CONJURERS_TALISMAN = registerItem("conjurers_talisman",
            (properties) -> new CurioBaseItem(properties.stacksTo(1)).withAttributes(Curios.NECKLACE_SLOT, new AttributeContainer(AttributeRegistry.SUMMON_DAMAGE, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    public static final Supplier<CurioBaseItem> GREATER_CONJURERS_TALISMAN = registerItem("greater_conjurers_talisman",
            (properties) -> new SimpleDescriptiveCurio(properties.stacksTo(1)).withAttributes(Curios.NECKLACE_SLOT, new AttributeContainer(AttributeRegistry.SUMMON_DAMAGE, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    public static final Supplier<CurioBaseItem> AFFINITY_RING = registerItem("affinity_ring",
            (properties) -> new AffinityRing(properties.stacksTo(1)));
    public static final Supplier<CurioBaseItem> CONCENTRATION_AMULET = registerItem("concentration_amulet",
            (properties) -> new SimpleDescriptiveCurio(properties.stacksTo(1), Curios.NECKLACE_SLOT));
    public static final Supplier<CurioBaseItem> LURKER_RING = registerItem("lurker_ring",
            (properties) -> new LurkerRing(new Item.Properties().stacksTo(1)));
    public static final Supplier<CurioBaseItem> AMETHYST_RESONANCE_NECKLACE = registerItem("amethyst_resonance_charm",
            (properties) -> new CurioBaseItem(properties.stacksTo(1)).withAttributes(Curios.NECKLACE_SLOT, new AttributeContainer(AttributeRegistry.MANA_REGEN, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    public static final Supplier<CurioBaseItem> INVISIBILITY_RING = registerItem("invisibility_ring",
            (properties) -> new InvisibiltyRing(properties.stacksTo(1)));
    public static final Supplier<CurioBaseItem> EXPULSION_RING = registerItem("expulsion_ring",
            (properties) -> new ExpulsionRing(new Item.Properties().stacksTo(1)));
    public static final Supplier<CurioBaseItem> VISIBILITY_RING = registerItem("visibility_ring",
            (properties) -> new VisibilityRing(properties.stacksTo(1)));
    public static final Supplier<CurioBaseItem> TELEPORTATION_AMULET = registerItem("teleportation_amulet",
            (properties) -> new TeleportationAmuletItem(properties.stacksTo(1).fireResistant()));
    public static final Supplier<CurioBaseItem> SIGNET_OF_THE_BETRAYER = registerItem("betrayer_signet",
            (properties) -> new BetrayerSignetRingItem(new Item.Properties().stacksTo(1).rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).fireResistant()).withAttributes(Curios.RING_SLOT, new AttributeContainer(AttributeRegistry.ELDRITCH_SPELL_POWER, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)));
    public static final Supplier<CurioBaseItem> WICKED_BONE_RING = registerItem("wicked_bone_ring",
            (properties) -> new WickedBoneRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));

    /**
     * Music Discs
     */
    public static final DeferredHolder<Item, Item> MUSIC_DISC_DEAD_KING_LULLABY = registerItem("music_disc_dead_king_lullaby",
            (properties) -> new Item(properties.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, IronsSpellbooks.id("dead_king_lullaby")))));
    public static final DeferredHolder<Item, Item> MUSIC_DISC_FLAME_STILL_BURNS = registerItem("music_disc_flame_still_burns",
            (properties) -> new Item(properties.stacksTo(1).rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue()).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, IronsSpellbooks.id("flame_still_burns")))));
    public static final DeferredHolder<Item, Item> FLAME_STILL_BURNS_FRAGMENT = registerItem("disc_fragment_flame_still_burns",
            (properties) -> new DiscFragmentItem(properties.rarity(CinderousRarity.CINDEROUS_RARITY_PROXY.getValue())));
    public static final DeferredHolder<Item, Item> MUSIC_DISC_WHISPERS_OF_ICE = registerItem("music_disc_whispers_of_ice",
            (properties) -> new Item(properties.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(ResourceKey.create(Registries.JUKEBOX_SONG, IronsSpellbooks.id("whispers_of_ice")))));

    /**
     * Spawn eggs
     */
    public static final Supplier<DeferredSpawnEggItem> KEEPER_SPAWN_EGG = registerItem("keeper_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.KEEPER, 0x352d2d, 0x766a76, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> DEAD_KING_CORPSE_SPAWN_EGG = registerItem("dead_king_corpse_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.DEAD_KING_CORPSE, 6842447, 15066584, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> ARCHEVOKER_SPAWN_EGG = registerItem("archevoker_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.ARCHEVOKER, 0x0C0C0C, 0xCCA858, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> NECROMANCER_SPAWN_EGG = registerItem("necromancer_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.NECROMANCER, 0x3E2B20, 0x515937, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> CRYOMANCER_SPAWN_EGG = registerItem("cryomancer_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.CRYOMANCER, 0xFFFFFF, 0x97ffed, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> PYROMANCER_SPAWN_EGG = registerItem("pyromancer_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.PYROMANCER, 0x7A1010, 0x262525, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> PRIEST_SPAWN_EGG = registerItem("priest_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.PRIEST, 0xFFFFFF, 0xffde58, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> APOTHECARIST_SPAWN_EGG = registerItem("apothecarist_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.APOTHECARIST, 0x37542a, 0xd49277, properties.stacksTo(64)));
    public static final Supplier<DeferredSpawnEggItem> ICE_SPIDER_SPAWN_EGG = registerItem("ice_spider_spawn_egg",
            (properties) -> new DeferredSpawnEggItem(EntityRegistry.ICE_SPIDER, 0x828192, 0xf5f5eb, properties.stacksTo(64)));

    public static Collection<DeferredHolder<Item, ? extends Item>> getIronsItems() {
        return ITEMS.getEntries();
    }

    /**
     * Item Registration Abstraction. Helpful in general, but especially to prepare for 26.1.2 Registration Changes
     */
    private static <T extends Item> DeferredHolder<Item, T> registerItem(String name, Function<Item.Properties, T> itemFactory) {
        return ITEMS.register(name, () -> itemFactory.apply(new Item.Properties()));
    }
}
