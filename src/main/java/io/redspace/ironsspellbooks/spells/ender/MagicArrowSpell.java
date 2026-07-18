package io.redspace.ironsspellbooks.spells.ender;

import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowProjectile;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowRenderer;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.client.render.SkillcastLevelRenderableManager;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.cast.EntityCasterRef;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class MagicArrowSpell extends AbstractSpell {

    /** Matches {@link MagicArrowProjectile#getBaseSpeed()}. */
    private static final float PROJECTILE_BASE_SPEED = 2.7f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public MagicArrowSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
        this.castTime = 30;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)));
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.MAGIC_ARROW_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.MAGIC_ARROW_RELEASE).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, PROJECTILE_BASE_SPEED);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        MagicArrowProjectile magicArrow = new MagicArrowProjectile(level, castContext.asEntityCaster());
        Vec3 origin = castContext.position(PositionAnchor.CASTING_POSITION);
        magicArrow.setPos(origin.subtract(0, magicArrow.getBoundingBox().getYsize() * 0.5f, 0).add(castContext.direction()));
        magicArrow.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, PROJECTILE_BASE_SPEED);
        magicArrow.shootFromContext(magicArrow, castContext);
        level.addFreshEntity(magicArrow);
    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        SkillcastLevelRenderableManager.track(castContext.caster(),
                (poseStack, buf, partialTick, casterRef, data, activeCast) -> {
                    if (casterRef instanceof EntityCasterRef entityCasterRef) {
                        // tranlsate to hand
                        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entityCasterRef.entity());
                        if (renderer instanceof LivingEntityRenderer livingEntityRenderer && livingEntityRenderer.getModel() instanceof HumanoidModel<?> humanoidModel) {
                            LivingEntity livingEntity = (LivingEntity) entityCasterRef.entity();
                            boolean mainhandIsLefthand = livingEntity instanceof Player player && player.getMainArm() == HumanoidArm.LEFT;
                            boolean leftHand = activeCast.context().getCastSource().isFromSlot(EquipmentSlot.OFFHAND) ^ mainhandIsLefthand;
                            poseStack.scale(1.0F, -1.0F, -1.0F);
                            float yaw = Mth.lerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
                            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
                            humanoidModel.translateToHand(leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT, poseStack);
                            poseStack.translate(((leftHand ? -1 : 1) / 32.0F), 1f, 0);
                            poseStack.mulPose(Axis.XP.rotationDegrees(90));
                        }
                    } else {
                        Vec3 renderDir = activeCast.context().direction();
                        float pitch = (float) -Math.asin(renderDir.y);
                        float yaw = (float) -Math.atan2(renderDir.x, renderDir.z);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180 - yaw * Mth.RAD_TO_DEG));
                        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch * Mth.RAD_TO_DEG));
                        poseStack.translate(0, 0, -1);
                    }
                    float scale = activeCast.completionPercent(casterRef.level().getGameTime(), partialTick);
                    scale = (float) Mth.smoothstep(Mth.clamp(scale + .3f, 0, 1));
                    poseStack.scale(scale, scale, scale);
                    MagicArrowRenderer.renderModel(poseStack, buf);
                }, false);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.BOW_CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.stop();
    }
}
