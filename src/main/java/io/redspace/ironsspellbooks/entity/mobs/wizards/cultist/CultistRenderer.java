package io.redspace.ironsspellbooks.entity.mobs.wizards.cultist;


import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cryomancer.CryomancerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CultistRenderer extends AbstractSpellCastingMobRenderer {

    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new CultistModel());
    }

}
