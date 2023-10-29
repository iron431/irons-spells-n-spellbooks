package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import software.bernie.shadowed.eliotlash.mclib.math.functions.limit.Max;

import java.util.function.Consumer;

public class ShockwaveParticle extends TextureSheetParticle {
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;
    private final float targetSize;

    ShockwaveParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, ShockwaveParticleOptions options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.targetSize = options.getScale();
        this.quadSize = 0;
        this.lifetime = (int) (targetSize * 2.5);
        this.gravity = .1f;

        float f = random.nextFloat() * 0.14F + 0.85F;
        this.rCol = options.getColor().x() * f;
        this.gCol = options.getColor().y() * f;
        this.bCol = options.getColor().z() * f;
        this.friction = 1;
    }

    @Override
    public float getQuadSize(float partialTick) {
        var f = (partialTick + this.age) / (float) this.lifetime;
        return Mth.lerp(1 - (1 - f) * (1 - f), 0, targetSize);
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
            this.yd -= 0.04D * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.yd *= .85f;
            this.xd *= .94f;
            this.zd *= .94f;
        }

    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialticks) {
        this.alpha = 1.0F - Mth.clamp((this.age + partialticks) / (float) this.lifetime, 0, 1F);

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
        pConsumer.vertex(pVec3f.x(), pVec3f.y() + .08f, pVec3f.z()).uv(p_233996_, p_233997_).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(p_233998_).endVertex();
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z).above();
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ShockwaveParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull ShockwaveParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            ShockwaveParticle shriekparticle = new ShockwaveParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
            shriekparticle.pickSprite(this.sprite);
            shriekparticle.setAlpha(1.0F);
            return shriekparticle;
        }
    }

}
