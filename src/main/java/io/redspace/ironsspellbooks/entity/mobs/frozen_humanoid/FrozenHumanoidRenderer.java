package io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid;


import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.IExtendedSimpleTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class FrozenHumanoidRenderer extends LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/frozen_humanoid.png");
    private static final ResourceLocation TEXTURE_ALT = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/frozen_humanoid_alt.png");
    final EntityModel<LivingEntity> originalModel;

    public FrozenHumanoidRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.36f);
        this.originalModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER));
    }

    boolean rectangular = false;

    @Override
    public void render(LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        LivingEntity entityToRender = entity;
        this.rectangular = false;
        if (entity instanceof FrozenHumanoid frozenHumanoid && frozenHumanoid.entityToCopy != null) {
            EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(frozenHumanoid.entityToCopy);
            var fakeEntity = frozenHumanoid.entityToCopy.create(Minecraft.getInstance().level);
            if (fakeEntity instanceof LivingEntity livingFakeEntity) {
                FrozenHumanoid.copyEntityVisualProperties(livingFakeEntity, entity);
                entityToRender = livingFakeEntity;
            }
            if (entityRenderer instanceof LivingEntityRenderer<?, ?> renderer) {
                this.model = (EntityModel<LivingEntity>) renderer.getModel();
                var texturelocation = ((LivingEntityRenderer) renderer).getTextureLocation(fakeEntity);
                var texture = Minecraft.getInstance().getTextureManager().getTexture(texturelocation);
                if (texture instanceof SimpleTexture) {
                    this.rectangular = ((IExtendedSimpleTexture) texture).irons_spellbooks$isRectangular();
                }
            }
        }
        try {
            super.render(entityToRender, entityYaw, partialTicks, poseStack, buffer, packedLight);
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("Failed to render Ice Shadow of {}: {}", ((FrozenHumanoid) entity).entityToCopy, e.getMessage());
            ((FrozenHumanoid) entity).entityToCopy = null;
        }
        this.rectangular = false;
        this.model = originalModel;
    }

    @Override
    protected boolean shouldShowName(LivingEntity entity) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        float f = entity.isCrouching() ? 32.0F : 64.0F;
        return d0 >= (double) (f * f) ? false : entity.isCustomNameVisible();
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntity pEntity) {
        return rectangular ? TEXTURE_ALT : TEXTURE;
    }

    @Override
    protected float getBob(LivingEntity pLivingBase, float pPartialTick) {
        return 0;
    }

}
