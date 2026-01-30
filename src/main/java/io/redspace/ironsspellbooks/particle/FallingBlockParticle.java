package io.redspace.ironsspellbooks.particle;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
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
        var bufs = Minecraft.getInstance().renderBuffers();
        var buf = bufs.bufferSource();
        for (Renderable r : toRender) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate((float) r.pos.x, (float) r.pos.y, (float) r.pos.z);
            BlockPos blockpos = BlockPos.containing(r.pos.x, r.pos.y + 1, r.pos.z); // trick lightning into being fullbright even with ground tremor blocks
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            var model = dispatcher.getBlockModel(r.state);
            //todo: implement original pos
            var originalpos = blockpos;
            for (var renderType : model.getRenderTypes(r.state, RandomSource.create(0), ModelData.EMPTY)) {
                dispatcher.getModelRenderer().tesselateBlock(
                        level, model, r.state, blockpos,
                        poseStack, buf.getBuffer(renderType), false,
                        RandomSource.create(), r.state.getSeed(originalpos),
                        OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
            }
            poseStack.popPose();

        }
        toRender.clear();
    }

    record Renderable(Vec3 pos, BlockState state) {
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
            float f = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - vec3.x());
            float f1 = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - vec3.y());
            float f2 = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - vec3.z());
            toRender.add(new Renderable(new Vec3(f, f1, f2), this.blockState));
//            toRender.add(()->{
//                var dispatcher = Minecraft.getInstance().getBlockRenderer();
//                Vec3 vec3 = camera.getPosition();
//                float f = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - vec3.x());
//                float f1 = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - vec3.y());
//                float f2 = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - vec3.z());
//                PoseStack poseStack = new PoseStack();
//                poseStack.translate(f, f1, f2);
//                BlockPos blockpos = BlockPos.containing(x, y + 1, z); // trick lightning into being fullbright even with ground tremor blocks
//                poseStack.translate(-0.5D, 0.0D, -0.5D);
//                var model = dispatcher.getBlockModel(blockState);
//                try {
//                    for (var renderType : model.getRenderTypes(blockState, RandomSource.create(0), ModelData.EMPTY))
//                        dispatcher.getModelRenderer().tesselateBlock(
//                                level, model, blockState, blockpos,
//                                poseStack, buffer, false,
//                                RandomSource.create(), blockState.getSeed(originalPos),
//                                OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
//                } catch (Exception e) {
//                    IronsSpellbooks.LOGGER.debug("irhngiernh");
//                    this.remove();
//                }
//            });

        }

    }
//
//    static final Direction[] DIRECTIONS = Direction.values();
//
//    public void tesselateWithAO(BlockAndTintGetter p_111079_, BakedModel p_111080_, BlockState p_111081_, BlockPos p_111082_, PoseStack p_111083_, VertexConsumer p_111084_, boolean p_111085_, RandomSource p_111086_, long p_111087_, int p_111088_, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType) {
//        float[] afloat = new float[DIRECTIONS.length * 2];
//        BitSet bitset = new BitSet(3);
//        ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
//        BlockPos.MutableBlockPos blockpos$mutableblockpos = p_111082_.mutable();
//
//        for(Direction direction : DIRECTIONS) {
//            p_111086_.setSeed(p_111087_);
//            List<BakedQuad> list = p_111080_.getQuads(p_111081_, direction, p_111086_, modelData, renderType);
//            if (!list.isEmpty()) {
//                blockpos$mutableblockpos.setWithOffset(p_111082_, direction);
//                if (!p_111085_ || Block.shouldRenderFace(p_111081_, p_111079_, p_111082_, direction, blockpos$mutableblockpos)) {
//                    this.renderModelFaceAO(p_111079_, p_111081_, p_111082_, p_111083_, p_111084_, list, afloat, bitset, modelblockrenderer$ambientocclusionface, p_111088_);
//                }
//            }
//        }
//
//        p_111086_.setSeed(p_111087_);
//        List<BakedQuad> list1 = p_111080_.getQuads(p_111081_, (Direction)null, p_111086_, modelData, renderType);
//        if (!list1.isEmpty()) {
//            this.renderModelFaceAO(p_111079_, p_111081_, p_111082_, p_111083_, p_111084_, list1, afloat, bitset, modelblockrenderer$ambientocclusionface, p_111088_);
//        }
//
//    }
//
//    private void renderModelFaceAO(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, PoseStack pPoseStack, VertexConsumer pConsumer, List<BakedQuad> pQuads, float[] pShape, BitSet pShapeFlags, ModelBlockRenderer.AmbientOcclusionFace pAoFace, int pPackedOverlay) {
//        for(BakedQuad bakedquad : pQuads) {
//            this.calculateShape(pLevel, pState, pPos, bakedquad.getVertices(), bakedquad.getDirection(), pShape, pShapeFlags);
//            if (!net.minecraftforge.client.ForgeHooksClient.calculateFaceWithoutAO(pLevel, pState, pPos, bakedquad, pShapeFlags.get(0), pAoFace.brightness, pAoFace.lightmap))
//                pAoFace.calculate(pLevel, pState, pPos, bakedquad.getDirection(), pShape, pShapeFlags, bakedquad.isShade());
//            this.putQuadData(pLevel, pState, pPos, pConsumer, pPoseStack.last(), bakedquad, pAoFace.brightness[0], pAoFace.brightness[1], pAoFace.brightness[2], pAoFace.brightness[3], pAoFace.lightmap[0], pAoFace.lightmap[1], pAoFace.lightmap[2], pAoFace.lightmap[3], pPackedOverlay);
//        }
//
//    }
//    private void putQuadData(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, VertexConsumer pConsumer, PoseStack.Pose pPose, BakedQuad pQuad, float pBrightness0, float pBrightness1, float pBrightness2, float pBrightness3, int pLightmap0, int pLightmap1, int pLightmap2, int pLightmap3, int pPackedOverlay) {
//        float f;
//        float f1;
//        float f2;
//        if (pQuad.isTinted()) {
//            int i = Minecraft.getInstance().getBlockRenderer().getModelRenderer().blockColors.getColor(pState, pLevel, pPos, pQuad.getTintIndex());
//            f = (float)(i >> 16 & 255) / 255.0F;
//            f1 = (float)(i >> 8 & 255) / 255.0F;
//            f2 = (float)(i & 255) / 255.0F;
//        } else {
//            f = 1.0F;
//            f1 = 1.0F;
//            f2 = 1.0F;
//        }
//
//        pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, new int[]{pLightmap0, pLightmap1, pLightmap2, pLightmap3}, pPackedOverlay, true);
//        pConsumer.vertex(pPose.pose(), )
//    }
//    private void calculateShape(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, int[] pVertices, Direction pDirection, @Nullable float[] pShape, BitSet pShapeFlags) {
//        float f = 32.0F;
//        float f1 = 32.0F;
//        float f2 = 32.0F;
//        float f3 = -32.0F;
//        float f4 = -32.0F;
//        float f5 = -32.0F;
//
//        for(int i = 0; i < 4; ++i) {
//            float f6 = Float.intBitsToFloat(pVertices[i * 8]);
//            float f7 = Float.intBitsToFloat(pVertices[i * 8 + 1]);
//            float f8 = Float.intBitsToFloat(pVertices[i * 8 + 2]);
//            f = Math.min(f, f6);
//            f1 = Math.min(f1, f7);
//            f2 = Math.min(f2, f8);
//            f3 = Math.max(f3, f6);
//            f4 = Math.max(f4, f7);
//            f5 = Math.max(f5, f8);
//        }
//
//        if (pShape != null) {
//            pShape[Direction.WEST.get3DDataValue()] = f;
//            pShape[Direction.EAST.get3DDataValue()] = f3;
//            pShape[Direction.DOWN.get3DDataValue()] = f1;
//            pShape[Direction.UP.get3DDataValue()] = f4;
//            pShape[Direction.NORTH.get3DDataValue()] = f2;
//            pShape[Direction.SOUTH.get3DDataValue()] = f5;
//            int j = DIRECTIONS.length;
//            pShape[Direction.WEST.get3DDataValue() + j] = 1.0F - f;
//            pShape[Direction.EAST.get3DDataValue() + j] = 1.0F - f3;
//            pShape[Direction.DOWN.get3DDataValue() + j] = 1.0F - f1;
//            pShape[Direction.UP.get3DDataValue() + j] = 1.0F - f4;
//            pShape[Direction.NORTH.get3DDataValue() + j] = 1.0F - f2;
//            pShape[Direction.SOUTH.get3DDataValue() + j] = 1.0F - f5;
//        }
//
//        float f9 = 1.0E-4F;
//        float f10 = 0.9999F;
//        switch (pDirection) {
//            case DOWN:
//                pShapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
//                pShapeFlags.set(0, f1 == f4 && (f1 < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
//                break;
//            case UP:
//                pShapeFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
//                pShapeFlags.set(0, f1 == f4 && (f4 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
//                break;
//            case NORTH:
//                pShapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
//                pShapeFlags.set(0, f2 == f5 && (f2 < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
//                break;
//            case SOUTH:
//                pShapeFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
//                pShapeFlags.set(0, f2 == f5 && (f5 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
//                break;
//            case WEST:
//                pShapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
//                pShapeFlags.set(0, f == f3 && (f < 1.0E-4F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
//                break;
//            case EAST:
//                pShapeFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
//                pShapeFlags.set(0, f == f3 && (f3 > 0.9999F || pState.isCollisionShapeFullBlock(pLevel, pPos)));
//        }
//
//    }

    public static final VertexFormat PARTICLE_BLOCK = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
            .put("UV1", DefaultVertexFormat.ELEMENT_UV1)
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
            .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
            .build());

    public static final ParticleRenderType BLOCK_RENDER_TYPE = new ParticleRenderType() {
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            builder.begin(VertexFormat.Mode.QUADS, PARTICLE_BLOCK);
        }

        public void end(Tesselator p_107444_) {
            p_107444_.end();
        }

        public String toString() {
            return "IRONS_SPELLBOOKS_BLOCK_RENDER_TYPE";
        }
    };

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return BLOCK_RENDER_TYPE;
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
