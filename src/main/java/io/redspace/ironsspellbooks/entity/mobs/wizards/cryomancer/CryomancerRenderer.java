package io.redspace.ironsspellbooks.entity.mobs.wizards.cryomancer;


import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CryomancerRenderer extends AbstractSpellCastingMobRenderer {

    public CryomancerRenderer(EntityRendererProvider.Context context) {
        super(context, new CryomancerModel());
    }

}
