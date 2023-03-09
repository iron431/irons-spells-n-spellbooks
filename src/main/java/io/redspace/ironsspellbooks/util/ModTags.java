package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static final TagKey<Item> SCHOOL_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"school_focus"));
    public static final TagKey<Item> FIRE_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"fire_focus"));
    public static final TagKey<Item> ICE_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"ice_focus"));
    public static final TagKey<Item> LIGHTNING_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"lightning_focus"));
    public static final TagKey<Item> ENDER_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"ender_focus"));
    public static final TagKey<Item> HOLY_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"holy_focus"));
    public static final TagKey<Item> BLOOD_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"blood_focus"));
    public static final TagKey<Item> EVOCATION_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"evocation_focus"));
    public static final TagKey<Item> VOID_FOCUS = ItemTags.create(new ResourceLocation(IronsSpellbooks.MODID,"void_focus"));

}
