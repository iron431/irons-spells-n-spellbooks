package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class FallingBlockParticle extends TextureSheetParticle {
    private final BlockState blockState;
    private final boolean particlesOnImpact;
    private final BlockPos originalPos;

    FallingBlockParticle(ClientLevel pLevel, double pX, double pY, double pZ, double xd, double yd, double zd, FallingBlockParticleOption options) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);
        this.xd = options.getMotion().x;
        this.yd = options.getMotion().y;
        this.zd = options.getMotion().z;

        this.lifetime = 200;
        this.quadSize = 1;

        this.blockState = options.getState();

        this.gravity = 0.08f;
        this.originalPos = BlockPos.containing(x, y, z);
        //todo: control over this?
        this.particlesOnImpact = false;
    }

    @Override
    public void tick() {
        //todo: idk how this works
        boolean onGround = this.onGround;
        age++;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        move(xd, yd, zd);
        yd -= gravity;
        if (this.blockState.isAir() || onGround || age > lifetime) {
            if (onGround) {
                if (particlesOnImpact) {
                    double speed = Math.sqrt(xd * xd + yd * yd + zd * zd);
                    for (int i = 0; i < 25; i++) {
                        Vec3 random = Utils.getRandomVec3(1).multiply(1, 0.25, 1).normalize().scale(speed * 10 + 0.1);
                        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.blockState), this.x, this.y, this.z, random.x, random.y, random.z);
                    }
                }
            }
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            var dispatcher = Minecraft.getInstance().getBlockRenderer();
            Vec3 vec3 = camera.getPosition();
            float f = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - vec3.x());
            float f1 = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - vec3.y());
            float f2 = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - vec3.z());
            PoseStack poseStack = new PoseStack();
            poseStack.translate(f, f1, f2);
            BlockPos blockpos = BlockPos.containing(x, y + 1, z); // trick lightning into being fullbright even with ground tremor blocks
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            var model = dispatcher.getBlockModel(blockState);
            for (var renderType : model.getRenderTypes(blockState, RandomSource.create(0), ModelData.EMPTY))
                dispatcher.getModelRenderer().tesselateBlock(
                        level, model, blockState, blockpos,
                        poseStack, buffer, false,
                        RandomSource.create(), blockState.getSeed(originalPos),
                        OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
        }

    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<FallingBlockParticleOption> {

        public Provider() {
        }

        public Particle createParticle(@NotNull FallingBlockParticleOption options, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new FallingBlockParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options);
        }
    }

}
