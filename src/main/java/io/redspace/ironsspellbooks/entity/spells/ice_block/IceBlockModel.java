package io.redspace.ironsspellbooks.entity.spells.ice_block;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class IceBlockModel extends AnimatedGeoModel<IceBlockProjectile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/ice_block.png");
    private static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/ice_block_projectile.geo.json");

    public IceBlockModel() {
    }

    @Override
    public ResourceLocation getTextureResource(IceBlockProjectile object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(IceBlockProjectile object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(IceBlockProjectile animatable) {
        return AbstractSpellCastingMob.animationInstantCast;
    }
}