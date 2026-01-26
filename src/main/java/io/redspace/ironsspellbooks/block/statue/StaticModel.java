package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.function.Function;

public class StaticModel extends GeoModel<StaticModel.Instance> {
    private final ResourceLocation model;
    private final ResourceLocation texture;

    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");

    public StaticModel(String modid, String name) {
        this(
                ResourceLocation.fromNamespaceAndPath(modid, String.format("geo/statue/%s.geo.json", name)),
                ResourceLocation.fromNamespaceAndPath(modid, String.format("textures/entity/statue/%s.png", name))
        );
    }


    public StaticModel(ResourceLocation model, ResourceLocation texture) {
        this.model = model;
        this.texture = texture;
    }

    public void render(PoseStack poseStack, Function<ResourceLocation, RenderType> renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        render(poseStack, renderType.apply(this.texture), bufferSource, packedLight, packedOverlay);
    }

    public void render(PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        BakedGeoModel model = this.getBakedModel(this.model);
        for (GeoBone group : model.topLevelBones()) {
            renderRecursively(poseStack, group, buffer, packedLight,
                    packedOverlay);
        }
    }

    private void renderRecursively(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);

        if (!bone.isHidden()) {
            for (GeoCube cube : bone.getCubes()) {
                poseStack.pushPose();
                renderCube(poseStack, cube, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            }
        }

        if (!bone.isHidingChildren()) {
            for (GeoBone childBone : bone.getChildBones()) {
                renderRecursively(poseStack, childBone, buffer, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private void renderCube(PoseStack poseStack, GeoCube cube, VertexConsumer buffer, int packedLight, int packedOverlay) {
        RenderUtil.translateToPivotPoint(poseStack, cube);
        RenderUtil.rotateMatrixAroundCube(poseStack, cube);
        RenderUtil.translateAwayFromPivotPoint(poseStack, cube);

        Matrix3f normalisedPoseState = poseStack.last().normal();
        Matrix4f poseState = new Matrix4f(poseStack.last().pose());

        for (GeoQuad quad : cube.quads()) {
            if (quad == null)
                continue;

            Vector3f normal = normalisedPoseState.transform(new Vector3f(quad.normal()));

            RenderUtil.fixInvertedFlatCube(cube, normal);
            createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay);
        }
    }

    private void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, VertexConsumer buffer, int packedLight, int packedOverlay) {
        for (GeoVertex vertex : quad.vertices()) {
            Vector3f position = vertex.position();
            Vector4f vector4f = poseState.transform(new Vector4f(position.x(), position.y(), position.z(), 1.0f));

            buffer.addVertex(vector4f.x(), vector4f.y(), vector4f.z(), -1, vertex.texU(),
                    vertex.texV(), packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
        }
    }

    public ResourceLocation getModelResource() {
        return model;
    }

    public ResourceLocation getTextureResource() {
        return texture;
    }

    @Override
    public ResourceLocation getModelResource(Instance animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(Instance animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(Instance animatable) {
        return ANIMATION;
    }

    public static class Instance implements GeoAnimatable {
        public static final Instance INSTANCE = new Instance();
        AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return cache;
        }

        @Override
        public double getTick(Object object) {
            return 0;
        }
    }
}
