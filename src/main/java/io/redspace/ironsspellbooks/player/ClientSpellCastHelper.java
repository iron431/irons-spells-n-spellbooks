package io.redspace.ironsspellbooks.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.holy.CloudOfRegenerationSpell;
import io.redspace.ironsspellbooks.spells.holy.FortifySpell;
import io.redspace.ironsspellbooks.spells.ice.FrostStepSpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.UUID;

import static io.redspace.ironsspellbooks.config.ClientConfigs.SHOW_FIRST_PERSON_ARMS;
import static io.redspace.ironsspellbooks.config.ClientConfigs.SHOW_FIRST_PERSON_ITEMS;

public class ClientSpellCastHelper {
    /**
     * Right Click Suppression
     */
    private static boolean suppressRightClicks;

    public static boolean shouldSuppressRightClicks() {
        return suppressRightClicks;
    }

    public static void setSuppressRightClicks(boolean suppressRightClicks) {
        //Ironsspellbooks.logger.debug("ClientSpellCastHelper.setSuppressRightClicks {}", suppressRightClicks);
        ClientSpellCastHelper.suppressRightClicks = suppressRightClicks;
    }

    /**
     * Handle Network Triggered Particles
     */
    public static void handleClientboundBloodSiphonParticles(Vec3 pos1, Vec3 pos2) {
        if (Minecraft.getInstance().player == null)
            return;
        var level = Minecraft.getInstance().player.level;
        Vec3 direction = pos2.subtract(pos1).scale(.1f);
        for (int i = 0; i < 40; i++) {
            Vec3 scaledDirection = direction.scale(1 + Utils.getRandomScaled(.35));
            Vec3 random = new Vec3(Utils.getRandomScaled(.08f), Utils.getRandomScaled(.08f), Utils.getRandomScaled(.08f));
            level.addParticle(ParticleHelper.BLOOD, pos1.x, pos1.y, pos1.z, scaledDirection.x + random.x, scaledDirection.y + random.y, scaledDirection.z + random.z);
        }
    }

    public static void handleClientsideHealParticles(Vec3 pos) {
        //Copied from arrow because these particles use their motion for color??
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            int i = PotionUtils.getColor(Potion.byName("healing"));
            double d0 = (double) (i >> 16 & 255) / 255.0D;
            double d1 = (double) (i >> 8 & 255) / 255.0D;
            double d2 = (double) (i >> 0 & 255) / 255.0D;

            for (int j = 0; j < 15; ++j) {
                level.addParticle(ParticleTypes.ENTITY_EFFECT, pos.x + Utils.getRandomScaled(0.25D), pos.y + Utils.getRandomScaled(1) + 1, pos.z + Utils.getRandomScaled(0.25D), d0, d1, d2);
            }
        }
    }

    public static void handleClientsideAbsorptionParticles(Vec3 pos) {
        //Copied from arrow because these particles use their motion for color??
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            int i = 16239960;//Copied from fortify's MobEffect registration (this is the color)
            double d0 = (double) (i >> 16 & 255) / 255.0D;
            double d1 = (double) (i >> 8 & 255) / 255.0D;
            double d2 = (double) (i >> 0 & 255) / 255.0D;

            for (int j = 0; j < 15; ++j) {
                level.addParticle(ParticleTypes.ENTITY_EFFECT, pos.x + Utils.getRandomScaled(0.25D), pos.y + Utils.getRandomScaled(1), pos.z + Utils.getRandomScaled(0.25D), d0, d1, d2);
            }
        }
    }

    public static void handleClientsideRegenCloudParticles(Vec3 pos) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = player.level;
            int ySteps = 16;
            int xSteps = 48;
            float yDeg = 180f / ySteps * Mth.DEG_TO_RAD;
            float xDeg = 360f / xSteps * Mth.DEG_TO_RAD;
            for (int x = 0; x < xSteps; x++) {
                for (int y = 0; y < ySteps; y++) {
                    Vec3 offset = new Vec3(0, 0, CloudOfRegenerationSpell.radius).yRot(y * yDeg).xRot(x * xDeg).zRot(-Mth.PI / 2).multiply(1, .85f, 1);
                    level.addParticle(DustParticleOptions.REDSTONE, pos.x + offset.x, pos.y + offset.y, pos.z + offset.z, 0, 0, 0);
                }
            }
        }
    }

    public static void handleClientsideFortifyAreaParticles(Vec3 pos) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = player.level;
            int ySteps = 128;
            float yDeg = 180f / ySteps * Mth.DEG_TO_RAD;
            for (int y = 0; y < ySteps; y++) {
                Vec3 offset = new Vec3(0, 0, FortifySpell.radius).yRot(y * yDeg);
                Vec3 motion = new Vec3(
                        Math.random() - .5,
                        Math.random() - .5,
                        Math.random() - .5
                ).scale(.1);
                level.addParticle(ParticleHelper.WISP, pos.x + offset.x, 1 + pos.y + offset.y, pos.z + offset.z, motion.x, motion.y, motion.z);
            }
        }
    }

    /**
     * Animation Helper
     */

    private static boolean didModify = false;

    private static void animatePlayerStart(Player player, ResourceLocation resourceLocation) {
        //IronsSpellbooks.LOGGER.debug("animatePlayerStart {} {}", player, resourceLocation);
        var keyframeAnimation = PlayerAnimationRegistry.getAnimation(resourceLocation);
        if (keyframeAnimation != null) {
            //noinspection unchecked
            var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(AbstractSpell.ANIMATION_RESOURCE);
            if (animation != null) {
                var castingAnimationPlayer = new KeyframeAnimationPlayer(keyframeAnimation);
                ClientMagicData.castingAnimationPlayerLookup.put(player.getUUID(), castingAnimationPlayer);
                var armsFlag = SHOW_FIRST_PERSON_ARMS.get();
                var itemsFlag = SHOW_FIRST_PERSON_ITEMS.get();

                if (armsFlag || itemsFlag) {
                    castingAnimationPlayer.setFirstPersonMode(/*resourceLocation.getPath().equals("charge_arrow") ? FirstPersonMode.VANILLA : */FirstPersonMode.THIRD_PERSON_MODEL);
                    castingAnimationPlayer.setFirstPersonConfiguration(new FirstPersonConfiguration(armsFlag, armsFlag, itemsFlag, itemsFlag));
                }

                //You might use  animation.replaceAnimationWithFade(); to create fade effect instead of sudden change
                animation.setAnimation(castingAnimationPlayer);
            }
        }
    }

    /**
     * Network Handling Wrapper
     */
    public static void handleClientboundOnClientCast(int spellId, int level, CastSource castSource, CastData castData) {
        var spell = AbstractSpell.getSpell(spellId, level);
        //IronsSpellbooks.LOGGER.debug("handleClientboundOnClientCast onClientCastComplete spell:{}", spell.getSpellType());

        spell.onClientCast(Minecraft.getInstance().player.level, Minecraft.getInstance().player, castData);
    }

    public static void handleClientboundTeleport(Vec3 pos1, Vec3 pos2) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            TeleportSpell.particleCloud(level, pos1);
            TeleportSpell.particleCloud(level, pos2);
        }
    }


    public static void handleClientboundFrostStep(Vec3 pos1, Vec3 pos2) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            FrostStepSpell.particleCloud(level, pos1);
            FrostStepSpell.particleCloud(level, pos2);
        }
    }

    public static void handleClientBoundOnCastStarted(UUID castingEntityId, SpellType spellType) {
        var player = Minecraft.getInstance().player.level.getPlayerByUUID(castingEntityId);
        var spell = AbstractSpell.getSpell(spellType, 1);
        //IronsSpellbooks.LOGGER.debug("handleClientBoundOnCastStarted {} {} {} {}", player, player.getUUID(), castingEntityId, spellType);

        spell.getCastStartAnimation().getForPlayer().ifPresent((resourceLocation -> animatePlayerStart(player, resourceLocation)));
        spell.onClientPreCast(player.level, player, player.getUsedItemHand(), null);

    }

    public static void handleClientBoundOnCastFinished(UUID castingEntityId, SpellType spellType, boolean cancelled) {
        //Ironsspellbooks.logger.debug("ClientSpellCastHelper.handleClientBoundOnCastFinished.1 -> ClientMagicData.resetClientCastState: {}", castingEntityId);
        ClientMagicData.resetClientCastState(castingEntityId);

        var player = Minecraft.getInstance().player.level.getPlayerByUUID(castingEntityId);
        AbstractSpell.getSpell(spellType, 1)
                .getCastFinishAnimation()
                .getForPlayer()
                .ifPresent((resourceLocation -> {
                    if (!cancelled) {
                        animatePlayerStart(player, resourceLocation);
                    }
                }));
    }

    public static void doAuraCastingParticles(LivingEntity entity, PoseStack poseStack, GeoModel model /*IBone rightHand, IBone rightArm*/) {

        var spellData = ClientMagicData.getSyncedSpellData(entity);

        if (spellData.getCastingSpellType() == SpellType.NONE_SPELL) {
            return;
        }
        var rightHand = model.getBone(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT).get();
        var rightArm = model.getBone("right_arm").get();
        var body = model.getBone("torso").get();
//        poseStack = new PoseStack();
//        poseStack.pushPose();
//        RenderUtils.translateToPivotPoint(poseStack, (GeoBone) rightHand);
//        RenderUtils.rotateMatrixAroundBone(poseStack, (GeoBone) rightArm);
//        RenderUtils.translateAwayFromPivotPoint(poseStack, (GeoBone) rightHand);
//        poseStack.translate(-(-1 / 32.0F - .125), .5, 0);
//        poseStack.translate(0, -rightHand.getPivotY() / 16, 0);
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
//        var m = poseStack.last().pose();
//
//        poseStack.popPose();
//        var m = poseStack.last().pose();
        var m = Matrix4f.createTranslateMatrix(0f, 0f, 0f);
        //var m = Matrix4f.createTranslateMatrix((float) entity.position().x, (float) entity.position().y, (float) entity.position().z);
        m.multiplyWithTranslation(rightHand.getPivotX() / 16f, rightHand.getPivotY() / 16f, rightHand.getPivotZ() / 16f);
        if (rightArm.getRotationZ() != 0.0F) {
            m.multiply(Vector3f.ZN.rotation(rightArm.getRotationZ()));
        }

        if (rightArm.getRotationY() != 0.0F) {
            m.multiply(Vector3f.YP.rotation(rightArm.getRotationY()));
        }

        if (rightArm.getRotationX() != 0.0F) {
            m.multiply(Vector3f.XN.rotation(rightArm.getRotationX()));
        }

        m.multiplyWithTranslation(-rightHand.getPivotX() / 16f, -rightHand.getPivotY() / 16f, -rightHand.getPivotZ() / 16f);
        //m.multiplyWithTranslation(-(-1 / 32.0F - .125f), .5f, 0);
        m.multiplyWithTranslation(0, -rightHand.getPivotY() / 16, 0);

        //        m.multiplyWithTranslation(-rightHand.getPivotX() / 16f, -rightHand.getPivotY() / 16f, -rightHand.getPivotZ() / 16f);
//        m.multiply(Vector3f.YP.rotationDegrees(180));
        //m.multiplyWithTranslation((float) entity.position().x, (float) entity.position().y, (float) entity.position().z);

//                Vec3 worldPos = new Vec3(leftHand.getPivotX() / 16f, leftHand.getPivotY() / 16f, leftHand.getPivotZ() / 16f);
//                worldPos = worldPos.zRot(leftArm.getRotationZ());
//                worldPos = worldPos.yRot(leftArm.getRotationY());
//                worldPos = worldPos.xRot(leftArm.getRotationX());
//                worldPos = worldPos.subtract(leftHand.getPivotX() / 16f, leftHand.getPivotY() / 16f, leftHand.getPivotZ() / 16f);
//                worldPos = worldPos.add(entity.position());
        Vec3 vec3 = new Vec3(m.m03, m.m13, m.m23)/*.yRot(Mth.PI)*/;
        var _x = /*m.m03*/vec3.x + entity.position().x;
        var _y = /*m.m13*/vec3.y + entity.position().y;
        var _z = /*m.m23*/vec3.z + entity.position().z;
        float radius = entity.getBbWidth() * .7f;
        int count = 16;
        for (int i = 0; i < count; i++) {
            if (entity.getRandom().nextFloat() < .25f) {
                double x, z;
                double theta = Math.toRadians(360 / count) * i/* + entity.tickCount*/;
                x = Math.cos(theta) * radius;
                z = Math.sin(theta) * radius;
                float speed = entity.getRandom().nextFloat() * .05f + .02f;

//                var _x = m.m03 + entity.position().x;
//                var _y = m.m13 + entity.position().y;
//                var _z = m.m23 + entity.position().z;

                entity.level.addParticle(getParticleFromSchool(spellData.getCastingSpellType().getSchoolType()), _x, _y, _z, 0, speed, 0);
                //entity.level.addParticle(getParticleFromSchool(spellData.getCastingSpellType().getSchoolType()), _x + x, _y, _z + z, 0, speed, 0);
            }
        }
    }

    public static ParticleOptions getParticleFromSchool(SchoolType school) {
        return switch (school) {
            case FIRE -> ParticleHelper.EMBERS;
            case ICE -> ParticleHelper.SNOWFLAKE;
            case LIGHTNING -> ParticleHelper.CASTING_LIGHTNING;
            case HOLY -> ParticleHelper.WISP;
            case ENDER -> ParticleHelper.CASTING_ENDER;
            case BLOOD -> ParticleHelper.CASTING_BLOOD;
            case POISON -> ParticleHelper.CASTING_POISON;
            case EVOCATION -> ParticleHelper.CASTING_EVOCATION;
            case VOID -> ParticleHelper.CASTING_VOID;
        };
    }
}
