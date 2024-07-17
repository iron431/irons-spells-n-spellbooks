package io.redspace.ironsspellbooks.player;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.gui.EldritchResearchScreen;
import io.redspace.ironsspellbooks.network.ClientboundCastErrorMessage;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.holy.CloudOfRegenerationSpell;
import io.redspace.ironsspellbooks.spells.holy.FortifySpell;
import io.redspace.ironsspellbooks.spells.ice.FrostStepSpell;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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

    public static void openEldritchResearchScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new EldritchResearchScreen(Component.empty(), hand));
    }

    public static void handleCastErrorMessage(ClientboundCastErrorMessage packet) {
        var spell = SpellRegistry.getSpell(packet.spellId);
        if (packet.errorType == ClientboundCastErrorMessage.ErrorType.COOLDOWN) {
            //ignore cooldown message if we are simply holding right click.
            if (ClientInputEvents.hasReleasedSinceCasting)
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("ui.irons_spellbooks.cast_error_cooldown", spell.getDisplayName(Minecraft.getInstance().player)).withStyle(ChatFormatting.RED), false);
        } else {
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("ui.irons_spellbooks.cast_error_mana", spell.getDisplayName(Minecraft.getInstance().player)).withStyle(ChatFormatting.RED), false);
        }
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

    public static void handleClientboundShockwaveParticle(Vec3 pos, float radius, ParticleType<?> particleType) {
        if (Minecraft.getInstance().player == null || !(particleType instanceof ParticleOptions)) {
            return;
        }
        var level = Minecraft.getInstance().player.level;
        int count = (int) (2 * Mth.PI * radius) * 2;
        float angle = 360f / count * Mth.DEG_TO_RAD;

        for (int i = 0; i < count; i++) {
            Vec3 motion = new Vec3(Mth.cos(angle * i) * radius, 0, Mth.sin(angle * i) * radius).scale(Utils.random.nextIntBetweenInclusive(50, 70) * .00155);
            level.addParticle((ParticleOptions) particleType, pos.x + motion.x * 4, pos.y, pos.z + motion.z * 4, motion.x, motion.y, motion.z);
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

    public static void handleClientboundOakskinParticles(Vec3 pos) {
        var player = Minecraft.getInstance().player;

        RandomSource randomsource = player.getRandom();
        for (int i = 0; i < 50; ++i) {
            double d0 = Mth.randomBetween(randomsource, -0.5F, 0.5F);
            double d1 = Mth.randomBetween(randomsource, 0F, 2f);
            double d2 = Mth.randomBetween(randomsource, -0.5F, 0.5F);
            var particleType = randomsource.nextFloat() < .1f ? ParticleHelper.FIREFLY : new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_WOOD.defaultBlockState());
            player.level.addParticle(particleType, pos.x + d0, pos.y + d1, pos.z + d2, d0 * .05, 0.05, d2 * .05);
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
            var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(SpellAnimations.ANIMATION_RESOURCE);
            if (animation != null) {
                var castingAnimationPlayer = new KeyframeAnimationPlayer(keyframeAnimation);
                ClientMagicData.castingAnimationPlayerLookup.put(player.getUUID(), castingAnimationPlayer);
                var armsFlag = SHOW_FIRST_PERSON_ARMS.get();
                var itemsFlag = SHOW_FIRST_PERSON_ITEMS.get();

                if (armsFlag || itemsFlag) {
                    castingAnimationPlayer.setFirstPersonMode(/*resourceLocation.getPath().equals("charge_arrow") ? FirstPersonMode.VANILLA : */FirstPersonMode.THIRD_PERSON_MODEL);
                    castingAnimationPlayer.setFirstPersonConfiguration(new FirstPersonConfiguration(armsFlag, armsFlag, itemsFlag, itemsFlag));
                } else {
                    castingAnimationPlayer.setFirstPersonMode(FirstPersonMode.DISABLED);
                }

                //You might use  animation.replaceAnimationWithFade(); to create fade effect instead of sudden change
                //animation.setAnimation(castingAnimationPlayer);
                animation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), castingAnimationPlayer, true);
            }
        }
    }

    /**
     * Network Handling Wrapper
     */
    public static void handleClientboundOnClientCast(String spellId, int level, CastSource castSource, ICastData castData) {
        var spell = SpellRegistry.getSpell(spellId);
        spell.onClientCast(Minecraft.getInstance().player.level, level, Minecraft.getInstance().player, castData);
    }

    public static void handleClientboundTeleport(Vec3 pos1, Vec3 pos2) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            TeleportSpell.particleCloud(level, pos1);
            TeleportSpell.particleCloud(level, pos2);
        }
    }

    public static void handleClientboundFieryExplosion(Vec3 pos, float radius) {
//        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(new Vector3f(1, .6f, 0.3f), explosionRadius + 2), x, y, z, 1, 0, 0, 0, 0, true);
//        MagicManager.spawnParticles(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 50 + (int) (25 * explosionRadius), explosionRadius * .25f, explosionRadius * .125f, explosionRadius * .25f, 0.03, true);
//        MagicManager.spawnParticles(level, ParticleHelper.EMBERS, x, y, z, 50, .25f * explosionRadius, .25f * explosionRadius, .25f * explosionRadius, 0.08, false);
//        MagicManager.spawnParticles(level, ParticleHelper.EMBERS, x, y, z, 100, .25f * explosionRadius, .25f * explosionRadius, .25f * explosionRadius, 0.2 + .1 * explosionRadius, false);
        MinecraftInstanceHelper.ifPlayerPresent(player -> {
            var level = player.level;
            var x = pos.x;
            var y = pos.y;
            var z = pos.z;
            //Blastwave
            level.addParticle(new BlastwaveParticleOptions(new Vector3f(1, .6f, 0.3f), radius + 1), x, y, z, 0, 0, 0);
            //Billowing wave
            int c = (int) (6.28 * radius) * 3;
            float step = 360f / c * Mth.DEG_TO_RAD;
            float speed = 0.06f + 0.01f * radius;
            for (int i = 0; i < c; i++) {
                Vec3 vec3 = new Vec3(Mth.cos(step * i), 0, Mth.sin(step * i)).scale(speed);
                Vec3 posOffset = Utils.getRandomVec3(.5f).add(vec3.scale(10));
                vec3 = vec3.add(Utils.getRandomVec3(0.01));
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x + posOffset.x, y + posOffset.y, z + posOffset.z, vec3.x, vec3.y, vec3.z);
            }
            //Smoke Cloud
            int cloudDensity = 50 + (int) (25 * radius);
            for (int i = 0; i < cloudDensity; i++) {
                Vec3 posOffset = Utils.getRandomVec3(1).scale(radius * .4f);
                Vec3 motion = posOffset.normalize().scale(speed * .5f);
                posOffset = posOffset.add(motion.scale(Utils.getRandomScaled(1)));
                motion = motion.add(Utils.getRandomVec3(0.02));
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
            }
            //Fire Cloud
            for (int i = 0; i < cloudDensity; i += 2) {
                Vec3 posOffset = Utils.getRandomVec3(1).scale(radius * .4f);
                Vec3 motion = posOffset.normalize().scale(speed * .5f);
                motion = motion.add(Utils.getRandomVec3(0.25));
                level.addParticle(ParticleHelper.EMBERS, true, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
                level.addParticle(ParticleHelper.FIRE, x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
            }
            //Sparks
            for (int i = 0; i < cloudDensity; i += 2) {
                Vec3 posOffset = Utils.getRandomVec3(radius).scale(.2f);
                Vec3 motion = posOffset.normalize().scale(0.6);
                motion = motion.add(Utils.getRandomVec3(0.18));
                level.addParticle(ParticleHelper.FIERY_SPARKS, x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
            }
        });
    }

    public static void handleClientboundFrostStep(Vec3 pos1, Vec3 pos2) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            FrostStepSpell.particleCloud(level, pos1);
            FrostStepSpell.particleCloud(level, pos2);
        }
    }

    public static void handleClientBoundOnCastStarted(UUID castingEntityId, String spellId, int spellLevel) {
        var player = Minecraft.getInstance().player.level.getPlayerByUUID(castingEntityId);
        var spell = SpellRegistry.getSpell(spellId);
        spell.getCastStartAnimation().getForPlayer().ifPresent((resourceLocation -> animatePlayerStart(player, resourceLocation)));
        spell.onClientPreCast(player.level, spellLevel, player, player.getUsedItemHand(), null);
    }

    public static void handleClientBoundOnCastFinished(UUID castingEntityId, String spellId, boolean cancelled) {
        ClientMagicData.resetClientCastState(castingEntityId);
        var player = Minecraft.getInstance().player.level.getPlayerByUUID(castingEntityId);

        var spell = SpellRegistry.getSpell(spellId);


        var finishAnimation = spell.getCastFinishAnimation();

        if (finishAnimation.getForPlayer().isPresent() && !cancelled) {
            animatePlayerStart(player, finishAnimation.getForPlayer().get());
        } else if (finishAnimation != AnimationHolder.pass() || cancelled) {
            var animationPlayer = ClientMagicData.castingAnimationPlayerLookup.getOrDefault(castingEntityId, null);
            if (animationPlayer != null) {
                animationPlayer.stop();
            }
        }

        if (cancelled && spell.stopSoundOnCancel()) {
            spell.getCastStartSound().ifPresent((soundEvent) -> Minecraft.getInstance().getSoundManager().stop(soundEvent.getLocation(), null));
        }

        if (castingEntityId.equals(Minecraft.getInstance().player.getUUID()) && ClientInputEvents.isUseKeyDown) {
            ClientInputEvents.hasReleasedSinceCasting = false;
        }
    }
}
