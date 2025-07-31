package io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer;


import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PyromancerRenderer extends AbstractSpellCastingMobRenderer {

    //public static ModelLayerLocation PYROMANCER_MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(irons_spellbooks.MODID, "pyromancer"), "body");
    //public static ModelLayerLocation PYROMANCER_INNER_ARMOR = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(irons_spellbooks.MODID, "pyromancer"), "inner_armor");
    //public static ModelLayerLocation PYROMANCER_OUTER_ARMOR = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(irons_spellbooks.MODID, "pyromancer"), "outer_armor");

    public PyromancerRenderer(EntityRendererProvider.Context context) {
        super(context, new PyromancerModel());
    }
}
