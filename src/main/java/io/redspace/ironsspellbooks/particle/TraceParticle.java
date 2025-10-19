package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class TraceParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    //    private final Vec3 forward, up;
    private final Vec3 destination;
    private final Vec3 forward;
    private final double speed;

    TraceParticle(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet spriteSet, double xd, double yd, double zd, TraceParticleOptions options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);


        this.lifetime = 4 + level.random.nextInt(5);
        this.gravity = 0;
        sprites = spriteSet;

        this.quadSize = 0.75f + level.random.nextFloat() * .25f;
        this.destination = new Vec3(options.destination.x, options.destination.y, options.destination.z);
        this.forward = destination.subtract(new Vec3(pX, pY, pZ)).normalize();
        this.speed = new Vec3(xd, yd, zd).length();
        this.rCol = options.color.x;
        this.gCol = options.color.y;
        this.bCol = options.color.z;

        this.friction = 1;
    }

    private Vec3 vec3Copy(Vector3f vector3f) {
        return new Vec3(vector3f.x, vector3f.y, vector3f.z);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.move(forward.x * speed, forward.y * speed, forward.z * speed);
        if (this.age++ > this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float f = (age + scaleFactor) / lifetime;
        f = f * f;
        return Mth.lerp(f, quadSize, quadSize * .5f);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());

        Vec3 ray = this.getPos().subtract(vec3).normalize();
        Vec3 forward = this.forward;
        Vec3 up = forward.cross(ray);


        Vector3f[] vertices = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        for (int i = 0; i < 4; i++) {
            float x = (float) (forward.x * vertices[i].x + up.x * vertices[i].y);
            float y = (float) (forward.y * vertices[i].x + up.y * vertices[i].y);
            float z = (float) (forward.z * vertices[i].x + up.z * vertices[i].y);
            vertices[i] = new Vector3f(x, y, z);
            vertices[i].mul(this.getQuadSize(partialTick));
            vertices[i].add(f, f1, f2);
        }
        int j = this.getLightColor(partialTick);
        this.makeCornerVertex(buffer, vertices[0], this.getU1(), this.getV1(), j);
        this.makeCornerVertex(buffer, vertices[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(buffer, vertices[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(buffer, vertices[3], this.getU0(), this.getV1(), j);
        //backface
        this.makeCornerVertex(buffer, vertices[3], this.getU0(), this.getV1(), j);
        this.makeCornerVertex(buffer, vertices[2], this.getU0(), this.getV0(), j);
        this.makeCornerVertex(buffer, vertices[1], this.getU1(), this.getV0(), j);
        this.makeCornerVertex(buffer, vertices[0], this.getU1(), this.getV1(), j);

    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVec3f, float p_233996_, float p_233997_, int p_233998_) {
        pConsumer.addVertex(pVec3f.x(), pVec3f.y(), pVec3f.z()).setUv(p_233996_, p_233997_).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(p_233998_);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<TraceParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        public Particle createParticle(@NotNull TraceParticleOptions options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            TraceParticle shriekparticle = new TraceParticle(pLevel, pX, pY, pZ, sprite, pXSpeed, pYSpeed, pZSpeed, options);
            shriekparticle.setSpriteFromAge(this.sprite);
            shriekparticle.setAlpha(1.0F);
            return shriekparticle;
        }
    }

}
