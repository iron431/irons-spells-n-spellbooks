package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class DeadKingModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king.png");
    public static final ResourceLocation TEXTURE_CORPSE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_resting.png");
    public static final ResourceLocation TEXTURE_ENRAGED = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_enraged.png");
    public static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/dead_king.geo.json");

    public DeadKingModel() {
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        if (object instanceof DeadKingBoss boss) {
            if (boss.isPhase(DeadKingBoss.Phases.FinalPhase))
                return TEXTURE_ENRAGED;
            else
                return TEXTURE_NORMAL;
        } else
            return TEXTURE_CORPSE;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        IBone jaw = this.getAnimationProcessor().getBone("jaw");
        IBone hair1 = this.getAnimationProcessor().getBone("hair");
        IBone hair2 = this.getAnimationProcessor().getBone("hair2");

        float f = entity.tickCount + animationEvent.getPartialTick();

        jaw.setRotationX(Mth.sin(f * .05f) * 5 * Mth.DEG_TO_RAD);
        hair1.setRotationX((Mth.sin(f * .1f) * 10 - 30) * Mth.DEG_TO_RAD);
        hair2.setRotationX(Mth.sin(f * .15f) * 15 * Mth.DEG_TO_RAD);

    }
}