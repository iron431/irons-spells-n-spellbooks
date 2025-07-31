package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ModTags {
    public static final TagKey<Item> SCHOOL_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "school_focus"));
    public static final TagKey<Item> FIRE_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "fire_focus"));
    public static final TagKey<Item> ICE_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "ice_focus"));
    public static final TagKey<Item> LIGHTNING_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "lightning_focus"));
    public static final TagKey<Item> ENDER_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "ender_focus"));
    public static final TagKey<Item> HOLY_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "holy_focus"));
    public static final TagKey<Item> BLOOD_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "blood_focus"));
    public static final TagKey<Item> EVOCATION_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "evocation_focus"));
    public static final TagKey<Item> ELDRITCH_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "eldritch_focus"));
    public static final TagKey<Item> NATURE_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "nature_focus"));
    public static final TagKey<Item> INSCRIBED_RUNES = ItemTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "inscribed_rune"));
    public static final TagKey<Block> SPECTRAL_HAMMER_MINEABLE = BlockTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "spectral_hammer_mineable"));
    public static final TagKey<Block> GUARDED_BY_WIZARDS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "guarded_by_wizards"));

    public static final TagKey<Structure> WAYWARD_COMPASS_LOCATOR = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "wayward_compass_locator"));

    public static final TagKey<EntityType<?>> ALWAYS_HEAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "always_heal"));
    public static final TagKey<EntityType<?>> CANT_ROOT = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "cant_root"));
    public static final TagKey<EntityType<?>> VILLAGE_ALLIES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "village_allies"));
    public static final TagKey<EntityType<?>> CANT_USE_PORTAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "cant_use_portal"));

    public static final TagKey<Biome> NO_DEFAULT_SPAWNS = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("forge", "no_default_monsters"));

    private static TagKey<DamageType> create(String tag) {
        return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, tag));
    }
}
