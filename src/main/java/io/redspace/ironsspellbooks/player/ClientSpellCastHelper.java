package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.gui.EldritchResearchScreen;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.holy.CloudOfRegenerationSpell;
import io.redspace.ironsspellbooks.spells.holy.FortifySpell;
import io.redspace.ironsspellbooks.spells.ice.FrostStepSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ClientSpellCastHelper {
      public static void openEldritchResearchScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new EldritchResearchScreen(Component.empty(), hand));
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

    public static void handleClientboundFlamethrowerParticles(Vec3 pos, Vec3 dir) {
        if (Minecraft.getInstance().player == null)
            return;
        var level = Minecraft.getInstance().player.level;
        int particleCount = dir.lengthSqr() < 0.25 ? 3 : 5;
        for (int i = 0; i < particleCount; i++) {
            Vec3 spread = Utils.getRandomVec3(0.025);
            Vec3 traverse = dir.scale(Utils.random.nextFloat()).add(pos);
            Vec3 motion = dir.scale(Utils.random.nextIntBetweenInclusive(8, 11) * .08f).add(spread);
            level.addParticle(ParticleHelper.FIRE_EMITTER, traverse.x, traverse.y, traverse.z, motion.x, motion.y, motion.z);
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
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            for (int j = 0; j < 15; ++j) {
                level.addParticle(coloredMobEffect(MobEffects.HEAL.value().getColor()), pos.x + Utils.getRandomScaled(0.25D), pos.y + Utils.getRandomScaled(1) + 1, pos.z + Utils.getRandomScaled(0.25D), Utils.getRandomScaled(0.005D), Utils.getRandomScaled(0.025D), Utils.getRandomScaled(0.005D));
            }
        }
    }

    public static ColorParticleOption coloredMobEffect(int color) {
        double d0 = (double) (color >> 16 & 255) / 255.0D;
        double d1 = (double) (color >> 8 & 255) / 255.0D;
        double d2 = (double) (color >> 0 & 255) / 255.0D;
        return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (float) d0, (float) d1, (float) d2);
    }

    public static void handleClientsideAbsorptionParticles(Vec3 pos) {
        //Copied from arrow because these particles use their motion for color??
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = Minecraft.getInstance().player.level;
            for (int j = 0; j < 15; ++j) {
                level.addParticle(coloredMobEffect(MobEffectRegistry.FORTIFY.get().getColor()), pos.x + Utils.getRandomScaled(0.5D), pos.y + Utils.getRandomScaled(1), pos.z + Utils.getRandomScaled(0.5D), 0, 0, 0);
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
                    Vec3 offset = new Vec3(0, 0, CloudOfRegenerationSpell.RADIUS).yRot(y * yDeg).xRot(x * xDeg).zRot(-Mth.PI / 2).multiply(1, .85f, 1);
                    level.addParticle(coloredMobEffect(MobEffects.HEAL.value().getColor()), pos.x + offset.x, pos.y + offset.y, pos.z + offset.z, 0, 0, 0);
                }
            }
        }
    }

    public static void handleClientsideFortifyAreaParticles(Vec3 pos) {
        var player = Minecraft.getInstance().player;

        if (player != null) {
            var level = player.level;
            int ySteps = 128;
            float yDeg = 360f / ySteps * Mth.DEG_TO_RAD;
            for (int y = 0; y < ySteps; y++) {
                Vec3 offset = new Vec3(0, 0, FortifySpell.RADIUS).yRot(y * yDeg);
                Vec3 motion = new Vec3(
                        Math.random() - .5,
                        Math.random() - .5,
                        Math.random() - .5
                ).scale(.1);
                level.addParticle(ParticleHelper.WISP, pos.x + offset.x, 1 + pos.y + offset.y, pos.z + offset.z, motion.x, motion.y, motion.z);
            }
        }
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
            float speed = (0.06f + 0.01f * radius) * 4f;
            for (int i = 0; i < c; i++) {
                Vec3 vec3 = new Vec3(Mth.cos(step * i), 0, Mth.sin(step * i)).scale(speed);
                Vec3 posOffset = Utils.getRandomVec3(.5f).add(vec3/*.scale(5)*/);
                vec3 = vec3.add(Utils.getRandomVec3(0.01));
                level.addParticle(ParticleHelper.FIERY_SMOKE, x + posOffset.x, y + posOffset.y, z + posOffset.z, vec3.x, vec3.y, vec3.z);
            }
            //Smoke Cloud
            int cloudDensity = 50 + (int) (25 * radius * Math.clamp(radius / 10, 1, 50));
            for (int i = 0; i < cloudDensity; i++) {
                Vec3 posOffset = Utils.getRandomVec3(1).scale(radius * .010f);
                Vec3 motion = posOffset.normalize().scale(speed * .5f);
                posOffset = posOffset.add(motion.scale(Utils.getRandomScaled(1)).normalize());
                motion = motion.add(Utils.getRandomVec3(speed * .2f * (i + cloudDensity) / (float) cloudDensity));
                level.addParticle(ParticleHelper.FIERY_SMOKE, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
            }
            int fireDensity = 50 + (int) (25 * radius);
            //Fire Cloud
            for (int i = 0; i < fireDensity; i += 2) {
                Vec3 posOffset = Utils.getRandomVec3(1).scale(radius * .4f);
                Vec3 motion = posOffset.normalize().scale(speed * .5f);
                motion = motion.add(Utils.getRandomVec3(0.25));
                level.addParticle(ParticleHelper.EMBERS, true, x + posOffset.x, y + posOffset.y, z + posOffset.z, motion.x, motion.y, motion.z);
                level.addParticle(ParticleHelper.FIRE, x + posOffset.x * .5f, y + posOffset.y * .5f, z + posOffset.z * .5f, motion.x, motion.y, motion.z);
            }
            //Sparks
            for (int i = 0; i < fireDensity; i += 2) {
                Vec3 posOffset = Utils.getRandomVec3(radius).scale(.2f);
                Vec3 motion = posOffset.normalize().scale(0.8);
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
}
