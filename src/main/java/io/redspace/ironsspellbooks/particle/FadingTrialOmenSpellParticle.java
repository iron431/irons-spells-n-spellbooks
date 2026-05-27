package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class FadingTrialOmenSpellParticle extends TextureSheetParticle {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet sprites;
    private float originalAlpha = 1.0F;

    protected FadingTrialOmenSpellParticle(
            ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites
    ) {
        super(level, x, y, z, 0.5 - RANDOM.nextDouble(), ySpeed, 0.5 - RANDOM.nextDouble());
        this.friction = 0.96F;
        this.gravity = -0.1F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        this.yd *= 0.2F;
        if (xSpeed == 0.0 && zSpeed == 0.0) {
            this.xd *= 0.1F;
            this.zd *= 0.1F;
        }

        this.quadSize *= 0.75F;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0F);
        }
        evaluateAlpha();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.isCloseToScopingPlayer()) {
            this.alpha = 0.0F;
        } else {
            this.alpha = Mth.lerp(0.05F, this.alpha, this.originalAlpha);
        }
        evaluateAlpha();
    }

    /**
     * Sets the particle alpha (float)
     */
    @Override
    protected void setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.originalAlpha = alpha;
    }

    private boolean isCloseToScopingPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localplayer = minecraft.player;
        return localplayer != null
                && localplayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0
                && minecraft.options.getCameraType().isFirstPerson()
                && localplayer.isScoping();
    }

    private void evaluateAlpha() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        float factor = (float) ((player.getEyePosition().distanceToSqr(this.x, this.y, this.z) - 1) / 3f);
        this.alpha = Mth.clamp(this.alpha * factor, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FadingTrialOmenSpellParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
