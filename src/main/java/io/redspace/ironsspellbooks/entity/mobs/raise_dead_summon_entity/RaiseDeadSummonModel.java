package io.redspace.ironsspellbooks.entity.mobs.raise_dead_summon_entity;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

public class RaiseDeadSummonModel extends AbstractSpellCastingMobModel {
    private static final ResourceLocation SKELETON_TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    private static final ResourceLocation ZOMBIE_TEXTURE = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation SKELETON_MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/skeleton_mob.geo.json");
    private static final ResourceLocation ZOMBIE_MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/abstract_casting_mob.geo.json");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return object instanceof RaiseDeadSummonEntity entity && entity.isZombie() ? ZOMBIE_TEXTURE : SKELETON_TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return object instanceof RaiseDeadSummonEntity entity && entity.isZombie() ? ZOMBIE_MODEL : SKELETON_MODEL;
    }
}