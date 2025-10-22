package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.undead_spawner;

import io.redspace.ironsspellbooks.entity.spells.portal.PortalRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class UndeadRiftRenderer extends PortalRenderer<UndeadRiftEntity> {

    public UndeadRiftRenderer(Context context) {
        super(context);
    }

    @Override
    protected PortalType getPortalType(UndeadRiftEntity entity) {
        return BLOOD;
    }
}