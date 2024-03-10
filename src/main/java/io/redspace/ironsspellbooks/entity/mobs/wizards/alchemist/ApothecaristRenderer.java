package io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist;


import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ApothecaristRenderer extends AbstractSpellCastingMobRenderer {

    public ApothecaristRenderer(EntityRendererProvider.Context context) {
        super(context, new ApothecaristModel());
    }

}
