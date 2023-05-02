package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FogParticle extends TextureSheetParticle {
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;

    FogParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, FogParticleOptions options) {
        super(pLevel, pX, pY, pZ, xd, yd, zd);
        this.quadSize = 1.5f * options.getScale();
        this.lifetime = pLevel.random.nextIntBetweenInclusive(60, 120);
        this.gravity = .2f;
        this.rCol = options.getColor().x();
        this.gCol = options.getColor().y();
        this.bCol = options.getColor().z();
        this.friction = 1;
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        return this.quadSize * (1 + Mth.clamp((this.age + pScaleFactor) / (float) this.lifetime * 0.75F, 0.0F, 1.0F));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd -= 0.04D * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.yd *= .7f;
            this.xd *= .98f;
            this.zd *= .98f;
        }

    }

    private float noise(float offset) {

        float f = 10 * Mth.sin(offset * .01f);
//        if (f > max) {
//            max = f;
//            IronsSpellbooks.LOGGER.debug("Min: {} | Max: {}", min, max);
//        }
//        if (f < min) {
//            min = f;
//            IronsSpellbooks.LOGGER.debug("Min: {} | Max: {}", min, max);
//        }
        return f;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialticks) {
        /*
        Copied from Shriek Particle
         */
        this.alpha = 1.0F - Mth.clamp(((float) this.age + partialticks - 20) / (float) this.lifetime, 0.2F, .7F);
        this.renderRotatedParticle(buffer, camera, partialticks, (p_234005_) -> {
            p_234005_.mul(Vector3f.YP.rotation(0));
            p_234005_.mul(Vector3f.XP.rotation(-DEGREES_90));
        });
        //back face?
        this.renderRotatedParticle(buffer, camera, partialticks, (p_234000_) -> {
            p_234000_.mul(Vector3f.YP.rotation(-(float) Math.PI));
            p_234000_.mul(Vector3f.XP.rotation(DEGREES_90));
        });
    }

    private void renderRotatedParticle(VertexConsumer pConsumer, Camera camera, float partialTick, Consumer<Quaternion> pQuaternion) {
        /*
        Copied from Shriek Particle
         */
        Vec3 vec3 = camera.getPosition();
        Vec3 zFightHack = camera.getPosition().subtract(this.x, this.y, this.z);
        zFightHack = zFightHack.multiply(1f, 0.75f, 1f);
        vec3 = zFightHack.add(this.x, this.y, this.z);
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        Quaternion quaternion = new Quaternion(ROTATION_VECTOR, 0.0F, true);

        pQuaternion.accept(quaternion);
        TRANSFORM_VECTOR.transform(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(partialTick);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
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
        Vec3 wiggle = new Vec3(noise((float) (age + this.x)), noise((float) (age - this.x)), noise((float) (age + this.z))).scale(.02f);
        pConsumer.vertex(pVec3f.x() + wiggle.x, pVec3f.y() + .08f + alpha * .125f, pVec3f.z() + wiggle.z).uv(p_233996_, p_233997_).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p_233998_).endVertex();
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<FogParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull FogParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            FogParticle shriekparticle = new FogParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
            shriekparticle.pickSprite(this.sprite);
            shriekparticle.setAlpha(1.0F);
            return shriekparticle;
        }
    }
}
