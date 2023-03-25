package io.redspace.ironsspellbooks.player;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.holy.CloudOfRegenerationSpell;
import io.redspace.ironsspellbooks.spells.holy.FortifySpell;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ClientSpellCastHelper {
    /**
     * Right Click Suppression
     */
    private static boolean suppressRightClicks;

    public static boolean shouldSuppressRightClicks() {
        return suppressRightClicks;
    }

    public static void setSuppressRightClicks(boolean suppressRightClicks) {
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
    private static void animatePlayerStart(Player player, ResourceLocation resourceLocation) {
        //IronsSpellbooks.LOGGER.debug("animatePlayerStart {} {}", player, resourceLocation);
        var keyframeAnimation = PlayerAnimationRegistry.getAnimation(resourceLocation);
        if (keyframeAnimation != null) {
            //noinspection unchecked
            var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(AbstractSpell.ANIMATION_RESOURCE);
            if (animation != null) {
                var castingAnimationPlayer = new KeyframeAnimationPlayer(keyframeAnimation);
                ClientMagicData.castingAnimationPlayerLookup.put(player.getUUID(), castingAnimationPlayer);
                castingAnimationPlayer.setFirstPersonMode(FirstPersonMode.DISABLED);

                //TODO: This should be configuration driven
                if (false) {
                    castingAnimationPlayer.setFirstPersonConfiguration(new FirstPersonConfiguration().setShowLeftArm(true).setShowRightArm(true));
                }

                //You might use  animation.replaceAnimationWithFade(); to create fade effect instead of sudden change
                animation.setAnimation(castingAnimationPlayer);
//                animation.addModifierLast(new AdjustmentModifier((partName) -> {
//                    float rotationX = 0;
//                    float rotationY = 0;
//                    float rotationZ = 0;
//                    float offsetX = 0;
//                    float offsetY = 0;
//                    float offsetZ = 0;
//                    var pitch = player.getXRot() / 2F;
//                    pitch = (float) Math.toRadians(pitch);
//                    switch (partName) {
////                        case "body" -> {
////                            rotationX = (-1F) * pitch;
////                        }
//                        case "rightArm", "leftArm" -> {
//                            rotationX = pitch;
//                        }
//                        default -> {
//                            return Optional.empty();
//                        }
//                    }
//                    return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(rotationX, rotationY, rotationZ), new Vec3f(offsetX, offsetY, offsetZ)));
//                }));

            }
        }
    }

    /**
     * Network Handling Wrapper
     */
    public static void handleClientboundOnClientCast(int spellId, int level, CastSource castSource) {
        var spell = AbstractSpell.getSpell(spellId, level);
        //IronsSpellbooks.LOGGER.debug("handleClientboundOnClientCast onClientCastComplete spell:{}", spell.getSpellType());
        spell.onClientCastComplete(Minecraft.getInstance().player.level, Minecraft.getInstance().player, null);
    }

    public static void handleClientboundTeleport(Vec3 pos1, Vec3 pos2) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            TeleportSpell.particleCloud(level, player, pos1);
            TeleportSpell.particleCloud(level, player, pos2);
        }
    }

    public static void handleClientBoundOnCastStarted(UUID castingEntityId, SpellType spellType) {
        var player = Minecraft.getInstance().player.level.getPlayerByUUID(castingEntityId);
        //IronsSpellbooks.LOGGER.debug("handleClientBoundOnCastStarted {} {} {} {}", player, player.getUUID(), castingEntityId, spellType);
        AbstractSpell.getSpell(spellType, 1)
                .getCastStartAnimation(player)
                .right()
                .ifPresent((resourceLocation -> animatePlayerStart(player, resourceLocation)));

    }

    public static void handleClientBoundOnCastFinished(UUID castingEntityId, SpellType spellType, boolean isCancelled) {
        var player = Minecraft.getInstance().player.level.getPlayerByUUID(castingEntityId);
        //IronsSpellbooks.LOGGER.debug("handleClientBoundOnCastFinished {} {} {} {}", player, player.getUUID(), castingEntityId, spellType);
        AbstractSpell.getSpell(spellType, 1)
                .getCastFinishAnimation(player)
                .right()
                .ifPresentOrElse(
                        (resourceLocation -> {
                            if (isCancelled) {
                                ClientMagicData.resetClientCastState(castingEntityId);
                            } else {
                                animatePlayerStart(player, resourceLocation);
                            }
                        }), //ifPresent
                        () -> ClientMagicData.resetClientCastState(castingEntityId) //orElse
                );

    }
}
