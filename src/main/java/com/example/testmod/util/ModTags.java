package com.example.testmod.util;

import com.example.testmod.TestMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static final TagKey<Item> SCHOOL_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"school_focus"));
    public static final TagKey<Item> FIRE_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"fire_focus"));
    public static final TagKey<Item> ICE_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"ice_focus"));
    public static final TagKey<Item> LIGHTNING_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"lightning_focus"));
    public static final TagKey<Item> ENDER_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"ender_focus"));
    public static final TagKey<Item> HOLY_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"holy_focus"));
    public static final TagKey<Item> BLOOD_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"blood_focus"));
    public static final TagKey<Item> EVOCATION_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"evocation_focus"));
    public static final TagKey<Item> VOID_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"void_focus"));

}
