package io.redspace.ironsspellbooks.entity.mobs.wizards.archevoker;


import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ArchevokerRenderer extends AbstractSpellCastingMobRenderer {

    public ArchevokerRenderer(EntityRendererProvider.Context context) {
        super(context, new ArchevokerModel());
    }
}
