package io.redspace.ironsspellbooks.entity.spells.creeper_head;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.WitherSkull;

public class CreeperHeadRenderer extends WitherSkullRenderer {
    ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/creeper_head.png");

    public CreeperHeadRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(WitherSkull pEntity) {
        return TEXTURE;
    }
}
