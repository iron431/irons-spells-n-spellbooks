package com.example.testmod.entity.mobs.dead_king_boss;


import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class DeadKingModel extends AbstractSpellCastingMobModel {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/dead_king/dead_king.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation(TestMod.MODID, "textures/entity/dead_king/dead_king_resting.png");
    private static final ResourceLocation TEXTURE3 = new ResourceLocation(TestMod.MODID, "textures/entity/dead_king/dead_king_enraged.png");
    private static final ResourceLocation MODEL = new ResourceLocation(TestMod.MODID, "geo/dead_king.geo.json");

    public DeadKingModel() {
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        if (object instanceof DeadKingBoss boss) {
            if (boss.isPhase(DeadKingBoss.Phases.FinalPhase))
                return TEXTURE3;
            else
                return TEXTURE;
        } else
            return TEXTURE2;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
    }
}