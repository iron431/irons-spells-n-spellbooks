package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Consumer;

public class RingSmokeParticle extends TextureSheetParticle {
    //private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f ROTATION_VECTOR = new Vector3f(0F, 0F, 0F);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private final float targetSize;
    private final boolean isFullbright;
    private float rx;
    private float ry;

    RingSmokeParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd/*, IShockwaveParticleOptions options*/) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);

        this.xd = xd * .7f;
        this.yd = yd * .7f;
        this.zd = zd * .7f;
        Vec3 deltaMovement = new Vec3(xd, yd, zd);
        rx = /*heading.x;*/(float) -Math.asin(yd / deltaMovement.length());
        ry = /*heading.y;*/ Mth.HALF_PI - (float) Mth.atan2(zd, xd);

        this.targetSize = 2.5f;//options.getScale();
        this.quadSize = .5f;
        this.lifetime = (int) (20 + Mth.absMax(0, targetSize - deltaMovement.length() * 5) * 20);
        this.gravity = 0f;

        float f = random.nextFloat() * 0.14F + 0.85F;
        this.rCol = 1;//options.color().x() * f;
        this.gCol = 1;//options.color().y() * f;
        this.bCol = 1;//options.color().z() * f;
        this.friction = 1;
        this.isFullbright = false;//options.isFullbright();
    }

    @Override
    public float getQuadSize(float partialTick) {
        var f = (partialTick + this.age) / (float) this.lifetime;
        return Mth.lerp(f/*1 - (1 - f) * (1 - f)*/, .15f, targetSize);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            //IronsSpellbooks.LOGGER.debug("Removing shockwave particle {}/{} ({}/{})", age, lifetime, getQuadSize(0), targetSize);
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.yd *= .99f;
            this.xd *= .99f;
            this.zd *= .99f;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialticks) {
//        IronsSpellbooks.LOGGER.debug("RingSmokeParticle.render heading: ({},{})", heading.x * Mth.RAD_TO_DEG, heading.y * Mth.RAD_TO_DEG);
        this.alpha = 1.0F - Mth.clamp((this.age + partialticks) / (float) this.lifetime, 0, 1F);
        //Quaternion rot = new Quaternion(rx, ry, 0, false);
        this.renderRotatedParticle(buffer, camera, partialticks, (quaternion) -> {
            //quaternion.mul(Vector3f.XP.rotation(-degrees90));
            quaternion.mul(Axis.YP.rotation(ry));
            quaternion.mul(Axis.XP.rotation(rx));
//            quaternion.mul(rot);
//            quaternion.mul(rot);

        });
        //back face
        this.renderRotatedParticle(buffer, camera, partialticks, (quaternion) -> {
            quaternion.mul(Axis.YP.rotation(ry));
            quaternion.mul(Axis.XP.rotation(Mth.PI));
            quaternion.mul(Axis.XP.rotation(rx));
            //quaternion.mul(Vector3f.YP.rotation(-(ry - degrees90)));
        });
    }

    private void renderRotatedParticle(VertexConsumer pConsumer, Camera camera, float partialTick, Consumer<Quaternionf> pQuaternion) {
        /*
        Copied from Shriek Particle
         */
        Vec3 vec3 = camera.getPosition();
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        Quaternionf quaternion = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());

        pQuaternion.accept(quaternion);
        //TRANSFORM_VECTOR.transform(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(partialTick);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        int j = this.getLightColor(partialTick);
        this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVec3f, float p_233996_, float p_233997_, int p_233998_) {
        pConsumer.vertex(pVec3f.x(), pVec3f.y(), pVec3f.z()).uv(p_233996_, p_233997_).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p_233998_).endVertex();
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        if (isFullbright) {
            return LightTexture.FULL_BRIGHT;
        }
        BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z).above();
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull SimpleParticleType options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            RingSmokeParticle shriekparticle = new RingSmokeParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed/*, options*/);
            shriekparticle.pickSprite(this.sprite);
            shriekparticle.setAlpha(1.0F);
            return shriekparticle;
        }
    }

}
