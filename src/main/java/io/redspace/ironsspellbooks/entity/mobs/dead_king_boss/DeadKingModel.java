package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;

public class DeadKingModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE_NORMAL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king.png");
    public static final ResourceLocation TEXTURE_CORPSE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_resting.png");
    public static final ResourceLocation TEXTURE_ENRAGED = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/dead_king/dead_king_enraged.png");
    public static final ResourceLocation TEXTURE_NORMAL_OMINOUS = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/dead_king/ominous/dead_king_ominous.png");
    public static final ResourceLocation TEXTURE_ENRAGED_OMINOUS = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/dead_king/ominous/dead_king_enraged_ominous.png");

    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/dead_king.geo.json");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        if (object instanceof DeadKingBoss boss) {
            boolean enraged = boss.isPhase(DeadKingBoss.Phases.FinalPhase);
            if (boss.isOminous()) {
                return enraged ? TEXTURE_ENRAGED_OMINOUS : TEXTURE_NORMAL_OMINOUS;
            } else {
                return enraged ? TEXTURE_ENRAGED : TEXTURE_NORMAL;

            }
        } else {
            return TEXTURE_CORPSE;
        }
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, long instanceId, AnimationState<AbstractSpellCastingMob> animationState) {
        float f = entity.tickCount + animationState.getPartialTick();

        if (entity instanceof DeadKingBoss boss && boss.isPhase(DeadKingBoss.Phases.FinalPhase)) {
            CoreGeoBone torso = this.getAnimationProcessor().getBone("torso");
            float torsoHeight = 18;
            float range = 20;
            float rotation = (Mth.sin(f * .05f) * range - range - 30) * Mth.DEG_TO_RAD / 2f;
            this.transformStack.pushRotation(torso, rotation, 0, 0);
            this.transformStack.pushPosition(torso, 0, torsoHeight * (Mth.cos(Mth.PI - rotation) + 1), 1 - torsoHeight * Mth.sin(Mth.PI - rotation));
        }
        super.setCustomAnimations(entity, instanceId, animationState);
        CoreGeoBone jaw = this.getAnimationProcessor().getBone("jaw");
        CoreGeoBone hair1 = this.getAnimationProcessor().getBone("hair");
        CoreGeoBone hair2 = this.getAnimationProcessor().getBone("hair2");

        //Builtin Resource Pack does not contain these bones
        if (jaw == null || hair1 == null || hair2 == null)
            return;

        jaw.setRotX(Mth.sin(f * .05f) * 5 * Mth.DEG_TO_RAD);
        hair1.setRotX((Mth.sin(f * .1f) * 10 - 30) * Mth.DEG_TO_RAD);
        hair2.setRotX(Mth.sin(f * .15f) * 15 * Mth.DEG_TO_RAD);

    }
}