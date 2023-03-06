package com.example.testmod.entity.mobs.wizards.pyromancer;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

public class PyromancerModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/pyromancer.png");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return TEXTURE;
    }

}