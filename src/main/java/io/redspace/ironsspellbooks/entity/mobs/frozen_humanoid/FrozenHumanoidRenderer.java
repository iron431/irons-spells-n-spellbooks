package io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

public class FrozenHumanoidRenderer extends LivingEntityRenderer<FrozenHumanoid, HumanoidModel<FrozenHumanoid>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/frozen_humanoid.png");

    public FrozenHumanoidRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.36f);

    }

    @Override
    public ResourceLocation getTextureLocation(FrozenHumanoid pEntity) {
        return TEXTURE;
    }

    @Override
    protected float getBob(FrozenHumanoid pLivingBase, float pPartialTick) {
        return 0;
    }

    @Override
    public void render(FrozenHumanoid iceMan, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<>(iceMan, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight)))
            return;
        pMatrixStack.pushPose();
        this.model.attackTime = this.getAttackAnim(iceMan, pPartialTicks);

        boolean shouldSit = iceMan.isSitting();
        this.model.riding = shouldSit;
        this.model.young = iceMan.isBaby();
        float bodyYRot = iceMan.yBodyRot;
        float yHeadRot = iceMan.yHeadRot;
        float f2 = yHeadRot - bodyYRot;
        if (shouldSit) {
            f2 = yHeadRot - bodyYRot;
            float f3 = Mth.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            bodyYRot = yHeadRot - f3;
            if (f3 * f3 > 2500.0F) {
                bodyYRot += f3 * 0.2F;
            }

            f2 = yHeadRot - bodyYRot;
        }

        float f6 = Mth.lerp(pPartialTicks, iceMan.xRotO, iceMan.getXRot());
        if (isEntityUpsideDown(iceMan)) {
            f6 *= -1.0F;
            f2 *= -1.0F;
        }

        if (iceMan.hasPose(Pose.SLEEPING)) {
            Direction direction = iceMan.getBedOrientation();
            if (direction != null) {
                float f4 = iceMan.getEyeHeight(Pose.STANDING) - 0.1F;
                pMatrixStack.translate((double) ((float) (-direction.getStepX()) * f4), 0.0D, (double) ((float) (-direction.getStepZ()) * f4));
            }
        }

        float bob = 0;
        this.setupRotations(iceMan, pMatrixStack, bob, bodyYRot, pPartialTicks);
        pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(iceMan, pMatrixStack, pPartialTicks);
        pMatrixStack.translate(0.0D, (double) -1.501F, 0.0D);
        //TODO: figure out why these numbers dont seem to match the base entity
        float limbSwingAmount = iceMan.getLimbSwingAmount();
        float limbSwing = iceMan.getLimbSwing();

//        limbSwing *= iceMan.getFrozenSpeed();
//        limbSwingAmount *= iceMan.getFrozenSpeed();


        this.model.prepareMobModel(iceMan, limbSwing, limbSwingAmount, pPartialTicks);
        this.model.setupAnim(iceMan, limbSwing, limbSwingAmount, bob, f2, f6);
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = this.isBodyVisible(iceMan);
        boolean flag1 = !flag && !iceMan.isInvisibleTo(minecraft.player);
        boolean flag2 = minecraft.shouldEntityAppearGlowing(iceMan);
        RenderType rendertype = this.getRenderType(iceMan, flag, flag1, flag2);
        if (rendertype != null) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
            int i = getOverlayCoords(iceMan, this.getWhiteOverlayProgress(iceMan, pPartialTicks));
            this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }

        pMatrixStack.popPose();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<>(iceMan, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight));
    }

    @Override
    protected float getAttackAnim(FrozenHumanoid pLivingBase, float pPartialTickTime) {
        return pLivingBase.getAttacktime();
    }
}
