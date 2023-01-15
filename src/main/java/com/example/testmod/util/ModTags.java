package com.example.testmod.util;

import com.example.testmod.TestMod;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

public class ModTags {
    public static final TagKey<Item> SCHOOL_FOCUS = ItemTags.create(new ResourceLocation(TestMod.MODID,"school_focus"));

}
