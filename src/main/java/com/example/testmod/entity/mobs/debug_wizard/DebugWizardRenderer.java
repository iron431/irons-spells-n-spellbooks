package com.example.testmod.entity.mobs.debug_wizard;


import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DebugWizardRenderer extends AbstractSpellCastingMobRenderer {

    //public static ModelLayerLocation PYROMANCER_MODEL_LAYER = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "pyromancer"), "body");
    //public static ModelLayerLocation PYROMANCER_INNER_ARMOR = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "pyromancer"), "inner_armor");
    //public static ModelLayerLocation PYROMANCER_OUTER_ARMOR = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "pyromancer"), "outer_armor");

    public DebugWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new AbstractSpellCastingMobModel() {
            @Override
            public ResourceLocation getTextureResource(AbstractSpellCastingMob mob) {
                return AbstractSpellCastingMob.textureResource;
            }
        });
    }
}