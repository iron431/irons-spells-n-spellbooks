package com.example.testmod.entity.mobs.dead_king_boss;


import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class DeadKingModel extends AbstractSpellCastingMobModel {
    private final boolean dormant;
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/dead_king/dead_king.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation(TestMod.MODID, "textures/entity/dead_king/dead_king_resting.png");
    private static final ResourceLocation MODEL = new ResourceLocation(TestMod.MODID, "geo/dead_king.geo.json");
    private static final ResourceLocation SPECIAL_ANIMATIONS = new ResourceLocation(TestMod.MODID, "animations/dead_king_animations.json");

    public DeadKingModel(boolean dormant) {
        this.dormant = dormant;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return dormant ? TEXTURE2 : TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
//        if (((DeadKingBoss) entity).isPhaseTransitioning())
//            return;
        super.setCustomAnimations(entity, instanceId, animationEvent);
    }
}