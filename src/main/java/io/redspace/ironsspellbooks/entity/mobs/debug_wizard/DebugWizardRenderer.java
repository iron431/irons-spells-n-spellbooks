package io.redspace.ironsspellbooks.entity.mobs.debug_wizard;


import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.render.DebugWizardSpellName;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DebugWizardRenderer extends AbstractSpellCastingMobRenderer {

    //public static ModelLayerLocation PYROMANCER_MODEL_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(irons_spellbooks.MODID, "pyromancer"), "body");
    //public static ModelLayerLocation PYROMANCER_INNER_ARMOR = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(irons_spellbooks.MODID, "pyromancer"), "inner_armor");
    //public static ModelLayerLocation PYROMANCER_OUTER_ARMOR = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(irons_spellbooks.MODID, "pyromancer"), "outer_armor");

    public DebugWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new AbstractSpellCastingMobModel() {
            @Override
            public ResourceLocation getTextureResource(AbstractSpellCastingMob mob) {
                return AbstractSpellCastingMob.textureResource;
            }
        });
        addRenderLayer(new DebugWizardSpellName(this));
    }
}