package com.example.testmod.entity.mobs.necromancer;


import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

public class NecromancerModel extends AbstractSpellCastingMobModel {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/necromancer.png");
    private static final ResourceLocation MODEL = new ResourceLocation(TestMod.MODID, "geo/skeleton_mob.geo.json");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return TEXTURE;
    }
    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }
}