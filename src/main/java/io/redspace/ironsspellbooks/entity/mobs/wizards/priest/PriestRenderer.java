package io.redspace.ironsspellbooks.entity.mobs.wizards.priest;


import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PriestRenderer extends AbstractSpellCastingMobRenderer {
    public PriestRenderer(EntityRendererProvider.Context context) {
        super(context, new PriestModel());
    }
}
