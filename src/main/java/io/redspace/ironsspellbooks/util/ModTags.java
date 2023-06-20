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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ModTags {
    public static final TagKey<Item> SCHOOL_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "school_focus"));
    public static final TagKey<Item> FIRE_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "fire_focus"));
    public static final TagKey<Item> ICE_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "ice_focus"));
    public static final TagKey<Item> LIGHTNING_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "lightning_focus"));
    public static final TagKey<Item> ENDER_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "ender_focus"));
    public static final TagKey<Item> HOLY_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "holy_focus"));
    public static final TagKey<Item> BLOOD_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "blood_focus"));
    public static final TagKey<Item> EVOCATION_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "evocation_focus"));
    public static final TagKey<Item> VOID_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "void_focus"));
    public static final TagKey<Item> POISON_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "poison_focus"));
    public static final TagKey<Item> CAN_BE_UPGRADED = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID, "can_be_upgraded"));
    public static final TagKey<Block> SPECTRAL_HAMMER_MINEABLE = BlockTags.create(new ResourceLocation(IronsSpellbooks.MODID, "spectral_hammer_mineable"));

    public static final TagKey<Structure> WAYWARD_COMPASS_LOCATOR = TagKey.create(Registries.STRUCTURE, new ResourceLocation(IronsSpellbooks.MODID, "wayward_compass_locator"));
    public static final TagKey<Structure> ANTIQUATED_COMPASS_LOCATOR = TagKey.create(Registries.STRUCTURE, new ResourceLocation(IronsSpellbooks.MODID, "antiquated_compass_locator"));
    public static final ResourceKey<Structure> MAGIC_AURA_TEMP = ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(IronsSpellbooks.MODID, "citadel"));

    public static final TagKey<EntityType<?>> ALWAYS_HEAL = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(IronsSpellbooks.MODID, "always_heal"));
    public static final TagKey<EntityType<?>> CANT_ROOT = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(IronsSpellbooks.MODID, "cant_root"));
    public static final TagKey<EntityType<?>> VILLAGE_ALLIES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(IronsSpellbooks.MODID, "village_allies"));

    public static final TagKey<DamageType> FIRE_MAGIC = create("fire_magic");
    public static final TagKey<DamageType> ICE_MAGIC = create("ice_magic");
    public static final TagKey<DamageType> LIGHTNING_MAGIC = create("lightning_magic");
    public static final TagKey<DamageType> HOLY_MAGIC = create("holy_magic");
    public static final TagKey<DamageType> ENDER_MAGIC = create("ender_magic");
    public static final TagKey<DamageType> BLOOD_MAGIC = create("blood_magic");
    public static final TagKey<DamageType> EVOCATION_MAGIC = create("evocation_magic");
    public static final TagKey<DamageType> VOID_MAGIC = create("void_magic");
    public static final TagKey<DamageType> POISON_MAGIC = create("poison_magic");

    private static TagKey<DamageType> create(String tag) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(IronsSpellbooks.MODID, tag));
    }
}
