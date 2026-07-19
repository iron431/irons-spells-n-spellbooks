package io.redspace.ironsspellbooks.spells.lightning;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceProjectile;
import io.redspace.ironsspellbooks.entity.spells.lightning_lance.LightningLanceRenderer;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
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
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LightningLanceSpell extends AbstractSpell {

    private static final float PROJECTILE_BASE_SPEED = 3f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public LightningLanceSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 14;
        this.spellPowerPerLevel = 2;
        this.castTime = 40;
        this.baseManaCost = 50;
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
        return PlayableSound.standard(SoundRegistry.LIGHTNING_LANCE_CAST).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.LIGHTNING_WOOSH_01).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, PROJECTILE_BASE_SPEED);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        LightningLanceProjectile lance = new LightningLanceProjectile(level, castContext.asEntityCaster());
        Vec3 origin = castContext.position(PositionAnchor.CASTING_POSITION);
        lance.setPos(origin.add(0, lance.getBoundingBox().getYsize() * 0.25f, 0).add(castContext.direction()));
        lance.shootFromContext(lance, castContext);
        level.addFreshEntity(lance);
    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        SkillcastLevelRenderableManager.track(castContext.caster(),
                (poseStack, buf, partialTick, casterRef, data, activeCast) -> {
                    if (casterRef instanceof EntityCasterRef entityCasterRef) {
                        if (Objects.equals(casterRef.get(), MinecraftInstanceHelper.getPlayer()) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                            return;
                        }
                        // tranlsate to hand
                        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entityCasterRef.entity());
                        boolean mainhandIsLefthand = entityCasterRef.entity() instanceof Player player && player.getMainArm() == HumanoidArm.LEFT;
                        boolean leftHand = activeCast.context().getCastSource().isFromSlot(EquipmentSlot.OFFHAND) ^ mainhandIsLefthand;
                        if (renderer instanceof LivingEntityRenderer livingEntityRenderer && livingEntityRenderer.getModel() instanceof HumanoidModel<?> humanoidModel) {
                            LivingEntity livingEntity = (LivingEntity) entityCasterRef.entity();
                            poseStack.scale(1.0F, -1.0F, -1.0F);
                            float yaw = Mth.lerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
                            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
                            humanoidModel.translateToHand(leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT, poseStack);
                            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                            float scale = livingEntity.getScale();
                            poseStack.scale(scale, scale, scale);
                            poseStack.translate(((leftHand ? -1 : 1) / 32.0F), 0.5f, 0);
                            poseStack.scale(1 / scale, 1 / scale, 1 / scale);
                        } else if (renderer instanceof GeoRenderer<?> geoRenderer && entityCasterRef.entity() instanceof LivingEntity livingEntity) {
                            String boneName = leftHand ? "bipedHandLeft" : "right_arm";
                            poseStack.mulPose(Axis.YP.rotationDegrees(180f - Mth.lerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot)));
                            poseStack.translate(0, 0.01f, 0);
                            Optional<GeoBone> hand = geoRenderer.getGeoModel().getBone(boneName);
                            if (hand.isPresent()) {
                                Vec3 offset = activeCast.context().position(PositionAnchor.ORIGIN).subtract(activeCast.context().position(PositionAnchor.CASTING_POSITION_CENTER));
                                poseStack.translate(offset.x, offset.y, offset.z);
                                // fixme: hardcoded mob scale factors (like dead king) bypass this. their (my) fault.
                                float scale = livingEntity.getScale();
                                poseStack.scale(scale, scale, scale);
                                setupPoseStackForBone(poseStack, hand.get());
                                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                                poseStack.translate(((leftHand ? -1 : 1) / 4F), .5f, 0);
                                poseStack.scale(1 / scale, 1 / scale, 1 / scale);
                                poseStack.translate(offset.x, offset.y, offset.z);
                            }
                        }
                    } else {
                        Vec3 renderDir = activeCast.context().direction();
                        float pitch = (float) -Math.asin(renderDir.y);
                        float yaw = (float) -Math.atan2(renderDir.x, renderDir.z);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180 - yaw * Mth.RAD_TO_DEG));
                        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch * Mth.RAD_TO_DEG));
                    }
                    float scale = activeCast.completionPercent(casterRef.level().getGameTime(), partialTick);
                    scale = (float) Mth.smoothstep(Mth.clamp(scale + .3f, 0, 1));
                    poseStack.scale(scale, scale, scale);
                    LightningLanceRenderer.renderModel(poseStack, buf, activeCast.elapsedTicks(casterRef.level().getGameTime()));
                }, false);
    }

    public static void setupPoseStackForBone(PoseStack poseStack, GeoBone start) {
        ArrayList<GeoBone> bones = new ArrayList<>();
        while (start != null) {
            bones.add(start);
            start = start.getParent();
        }
        for (int i = bones.size() - 1; i >= 0; i--) {
            var bone = bones.get(i);
            RenderUtil.prepMatrixForBone(poseStack, bone);
        }
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CHARGED_CAST;
    }
}
