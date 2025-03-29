package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
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
        // 1 -- 3
        // |    |  <- index map relative to forward
        // 0 -- 2
//        Vec3[] vx = Arrays.stream(entity.cornerPins).map(v -> v.subtract(entity.position())).toArray(Vec3[]::new);
//        Vec3 n0 = vx[1].subtract(vx[0]).cross(vx[2].subtract(vx[0]));
//        Vec3 n1 = vx[3].subtract(vx[1]).cross(vx[0].subtract(vx[1]));
//        Vec3 n2 = vx[0].subtract(vx[2]).cross(vx[3].subtract(vx[2]));
//        Vec3 n3 = vx[2].subtract(vx[3]).cross(vx[1].subtract(vx[3]));
//        Vec3 normal = n0.add(n1).add(n2).add(n3).scale(0.25);
        Vec3 normal = Utils.lerp(partialTick, entity.lastNormal, entity.normal);
        poseStack.mulPose(rotationBetweenVectors(new Vector3f(0, 1, 0), cast(normal)));
    }

    public static Quaternionf rotationBetweenVectors(Vector3f from, Vector3f to) {
        // thanks yeepeetee
        Vector3f fromNorm = new Vector3f(from).normalize();
        Vector3f toNorm = new Vector3f(to).normalize();

        float dot = fromNorm.dot(toNorm);

        if (dot >= 0.9999f) { // Vectors are nearly identical
            return new Quaternionf().identity();
        } else if (dot <= -0.9999f) { // Vectors are opposite
            // Find an arbitrary perpendicular vector
            Vector3f perpendicular = new Vector3f(1, 0, 0);
            if (Math.abs(fromNorm.x) > 0.9f) {
                perpendicular.set(0, 1, 0);
            }
            perpendicular.cross(fromNorm).normalize();
            return new Quaternionf().rotationAxis((float) Math.PI, perpendicular);
        }

        // Compute rotation axis and angle
        Vector3f axis = new Vector3f(fromNorm).cross(toNorm).normalize();
        float angle = (float) Math.acos(dot);

        return new Quaternionf().rotationAxis(angle, axis);
    }

    private Vector3f cast(Vec3 vec3) {
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }
}
