package io.redspace.ironsspellbooks.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class FallingBlockParticle extends TextureSheetParticle {
    private final BlockState blockState;
    private final boolean particlesOnImpact;
    private final BlockPos originalPos;

    @SubscribeEvent
    public static void globalrender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        var level = Minecraft.getInstance().level;
        if (level == null) {
            toRender.clear();
            return;
        }
        synchronized (toRender) {
            if (toRender.isEmpty()) {
                return;
            }
            var bufs = Minecraft.getInstance().renderBuffers();
            var buf = bufs.bufferSource();
            for (Renderable particle : toRender) {
                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();
                poseStack.translate((float) particle.relativePos.x, (float) particle.relativePos.y, (float) particle.relativePos.z);
                BlockPos blockpos = particle.worldPos.above(); // trick lightning into being at above-ground levels even with in-ground tremor blocks
                poseStack.translate(-0.5D, 0.0D, -0.5D);
                var model = dispatcher.getBlockModel(particle.state);
                for (var renderType : model.getRenderTypes(particle.state, RandomSource.create(0), ModelData.EMPTY)) {
                    dispatcher.getModelRenderer().tesselateBlock(
                            level, model, particle.state, blockpos,
                            poseStack, buf.getBuffer(renderType), false,
                            RandomSource.create(), particle.state.getSeed(particle.originalPos),
                            OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
                }
                poseStack.popPose();

            }
            toRender.clear();
        }
    }

    record Renderable(BlockPos worldPos, BlockPos originalPos, Vec3 relativePos, BlockState state) {
    }

    private static final List<Renderable> toRender = new ArrayList<>();

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
            Vec3 vec3 = camera.getPosition();
            float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
            float f1 = (float) (Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
            float f2 = (float) (Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
            toRender.add(new Renderable(BlockPos.containing(x, y, z), this.originalPos, new Vec3(f, f1, f2), this.blockState));
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
