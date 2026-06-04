package io.redspace.ironsspellbooks.entity.spells.ender_chain;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.RenderHelper;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EnderChainRenderer extends EntityRenderer<EnderChain> {
    public static final ResourceLocation CHAIN_TEXTURE = IronsSpellbooks.id("textures/entity/ender_chain.png");

    public EnderChainRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public void render(EnderChain entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        PartEntity<?>[] parts = entity.getParts();
        if (parts == null || parts.length == 0) {
            return;
        }
        PartEntity<?> last = parts[parts.length - 1];
        Vec3 lastWorldpos = new Vec3(Mth.lerp(partialTick, last.xo, last.getX()), Mth.lerp(partialTick, last.yo, last.getY()), Mth.lerp(partialTick, last.zo, last.getZ()));
        Vec3 targetPos = lastWorldpos.subtract(entity.position());
        float warmup = Mth.clamp((entity.warmup + partialTick) / EnderChain.VISUAL_WARMUP_TIME, 0, 1f);
        renderChainBetween(Vec3.ZERO, targetPos, poseStack, bufferSource, warmup);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    public static void renderChainBetween(Vec3 start, Vec3 to, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        renderChainBetween(start, to, poseStack, multiBufferSource, 1f);
    }

    public static void renderChainBetween(Vec3 start, Vec3 to, PoseStack poseStack, MultiBufferSource multiBufferSource, float warmupPercent) {
        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);

        var consumer = multiBufferSource.getBuffer(RenderHelper.CustomerRenderType.magicNoCull(CHAIN_TEXTURE));
        Vec3 delta = to.subtract(start);
        float distance = (float) delta.length();
        if (distance < 1.0E-4f) {
            poseStack.popPose();
            return;
        }
        Vec3 direction = delta.normalize();
        Quaternionf rotation = new Quaternionf().rotationTo(
                new Vector3f(0, 0, 1),
                new Vector3f((float) direction.x, (float) direction.y, (float) direction.z)
        );
        poseStack.mulPose(rotation);

        PoseStack.Pose pose = poseStack.last();
        Vec3 origin = Vec3.ZERO;
        Vec3 destination = new Vec3(0, 0, distance);
        if (warmupPercent < 1) {
            origin = destination.scale(1 - warmupPercent);
        }
        SpellRenderingHelper.drawQuad(origin, destination, 1, 0, pose, consumer, 255, 255, 255, 255, 0, distance);
        SpellRenderingHelper.drawQuad(origin, destination, 0, 1, pose, consumer, 255, 255, 255, 255, 0, distance);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EnderChain entity) {
        return CHAIN_TEXTURE;
    }


}
