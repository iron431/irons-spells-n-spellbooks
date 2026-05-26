package io.redspace.ironsspellbooks.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FadingTrialOmenSpellParticle extends SpellParticle {
    public FadingTrialOmenSpellParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        evaluateAlpha();
    }

    @Override
    public void tick() {
        super.tick();
        evaluateAlpha();
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
}
