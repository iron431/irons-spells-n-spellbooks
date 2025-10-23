package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.mixin.ParticleAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwirlingParticle extends Particle {
    final Vec3 origin;
    @Nullable
    final Particle particle;
    final SwirlingParticleOptions options;
    double width, height, speed;
    double dWidth, dHeight, dSpeed;
    Vec3 u, v;
    float cycle;

    public SwirlingParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, double xd, double yd, double zd, SwirlingParticleOptions options) {
        super(level, xCoord, yCoord, zCoord, 0, 0, 0);


        this.options = options;
        this.u = options.up();
        this.v = this.u.cross(options.normal());

        this.origin = new Vec3(xCoord, yCoord, zCoord);

        this.scale(this.random.nextFloat() * 1.75f + 1f);
        this.lifetime = 40 + (int) (Math.random() * 45);
        this.cycle = this.random.nextInt(360);

        this.height = options.heightWidthSpeed().x;
        this.width = options.heightWidthSpeed().y;
        this.speed = options.heightWidthSpeed().z;
        this.dHeight = options.deltaHeightWidthSpeed().x;
        this.dWidth = options.deltaHeightWidthSpeed().y;
        this.dSpeed = options.deltaHeightWidthSpeed().z;
        ParticleOptions particleOptions = options.particleOptions();

        this.particle = Minecraft.getInstance().particleEngine.createParticle(particleOptions, xCoord, yCoord, zCoord, 0, 0, 0);
        if (particle != null) {
            Vec3 pos = calculatePos();
            particle.setPos(pos.x, pos.y, pos.z);
            // help with potential jitter
            ((ParticleAccessor) particle).irons_spellbooks$gravity(0);
            // trick basic particles into ceasing their movement
            ((ParticleAccessor) particle).irons_spellbooks$stoppedByCollision(true);
            this.tick();
        }

    }

    @Override
    public void tick() {
        if (particle == null) {
            this.remove();
            return;
        }
        Vec3 oldpos = particle.getPos();
        if (!particle.isAlive()) {
            this.remove();
            return;
        }
        cycle = cycle + (float) speed;
        while (cycle >= 360) {
            cycle -= 360;
        }
        Vec3 pos = calculatePos();
        particle.setPos(pos.x, pos.y, pos.z);
        ((ParticleAccessor) particle).irons_spellbooks$xo(oldpos.x);
        ((ParticleAccessor) particle).irons_spellbooks$yo(oldpos.y);
        ((ParticleAccessor) particle).irons_spellbooks$zo(oldpos.z);
        height += dHeight;
        width += dWidth;
        speed += dSpeed;
    }

    private Vec3 calculatePos() {
        float f = cycle * Mth.DEG_TO_RAD;
        return origin
                .add(u.scale(height * Mth.cos(f) * 0.5))
                .add(v.scale(width * Mth.sin(f) * 0.5));
    }


    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {

    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SwirlingParticleOptions> {

        public Provider() {
        }

        public Particle createParticle(@NotNull SwirlingParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new SwirlingParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
        }
    }
}
