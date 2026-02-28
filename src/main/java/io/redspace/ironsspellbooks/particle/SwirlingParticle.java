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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwirlingParticle extends Particle {
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
        this.v = options.up().cross(options.normal());
        this.u = options.normal().cross(v);

        this.x = xCoord;
        this.y = yCoord;
        this.z = zCoord;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

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
        move(xd, yd, zd);
    }

    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0);

//    public void move(double x, double y, double z) {
//        if (!((ParticleAccessor) this).irons_spellbooks$isStoppedByCollision()) {
//            double d0 = x;
//            double d1 = y;
//            double d2 = z;
//            if (this.hasPhysics
//                    && (x != 0.0 || y != 0.0 || z != 0.0)
//                    && x * x + y * y + z * z < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
//                Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, List.of());
//                if (x != vec3.x || y != vec3.y || z != vec3.z) {
//                    this.remove();
//                    // release particle
//                    if (particle != null) {
//                        Vec3 p1 = calculatePos();
//                        cycle += speed;
//                        Vec3 p2 = calculatePos();
//                        Vec3 motion = p2.subtract(p1).add(new Vec3(xd, yd, zd).scale(0.5));
//                        ((ParticleAccessor) particle).irons_spellbooks$stoppedByCollision(false);
//                        ((ParticleAccessor) particle).irons_spellbooks$xd(motion.x);
//                        ((ParticleAccessor) particle).irons_spellbooks$yd(motion.y);
//                        ((ParticleAccessor) particle).irons_spellbooks$zd(motion.z);
//                    }
//                    return;
//                }
//            }
//
//            if (x != 0.0 || y != 0.0 || z != 0.0) {
//                this.setBoundingBox(this.getBoundingBox().move(x, y, z));
//                this.setLocationFromBoundingbox();
//            }
//        }
//    }

    private Vec3 calculatePos() {
        float f = cycle * Mth.DEG_TO_RAD;
        return this.getPos()
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
