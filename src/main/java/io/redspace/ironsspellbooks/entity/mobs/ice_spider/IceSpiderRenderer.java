package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IceSpiderRenderer extends GeoEntityRenderer<IceSpiderEntity> {
    public IceSpiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new IceSpiderModel());
    }

    @Override
    public void preRender(PoseStack poseStack, IceSpiderEntity entity, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        Vec3 normal = Utils.lerp(partialTick, entity.lastNormal, entity.normal);
        poseStack.mulPose(Utils.rotationBetweenVectors(new Vector3f(0, 1, 0), cast(normal)));
    }

    @Override
    protected float getDeathMaxRotation(IceSpiderEntity animatable) {
        return 180f;
    }

    private Vector3f cast(Vec3 vec3) {
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }
}
