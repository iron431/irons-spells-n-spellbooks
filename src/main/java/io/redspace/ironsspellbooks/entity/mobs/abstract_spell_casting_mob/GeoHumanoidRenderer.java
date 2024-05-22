package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.example.client.EntityResources;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.item.GeoArmorItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

import java.util.List;

public class GeoHumanoidRenderer<T extends Mob & IAnimatable> extends ExtendedGeoEntityRenderer<T> {
    private ResourceLocation textureResource;

    public GeoHumanoidRenderer(EntityRendererProvider.Context renderManager, AnimatedGeoModel<T> model) {
        super(renderManager, model);
        this.shadowRadius = 0.5f;
    }

    @Nullable
    @Override
    protected ResourceLocation getTextureForBone(String boneName, T animatable) {
        if ("bipedCape".equals(boneName))
            return EntityResources.EXTENDED_CAPE_TEXTURE;

        return modelProvider.getTextureResource(animatable);
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
//        boolean f = bone.getName().startsWith("armor") || bone.getName().equals(GenericCustomArmorRenderer.leggingTorsoLayerBone);
//        IronsSpellbooks.LOGGER.debug("GeoHumanoidRenderer.isArmorBone: {} - {}",bone.getName(),f);

        return bone.getName().startsWith("armor");
    }

    @Override
    protected ModelPart getArmorPartForBone(String name, HumanoidModel<?> armorModel) {
        return switch (name) {
            case DefaultBipedBoneIdents.LEFT_FOOT_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.LEFT_LEG_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.LEFT_FOOT_ARMOR_BONE_2_IDENT,
                    DefaultBipedBoneIdents.LEFT_LEG_ARMOR_BONE_2_IDENT -> armorModel.leftLeg;
            case DefaultBipedBoneIdents.RIGHT_FOOT_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.RIGHT_LEG_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.RIGHT_FOOT_ARMOR_BONE_2_IDENT,
                    DefaultBipedBoneIdents.RIGHT_LEG_ARMOR_BONE_2_IDENT -> armorModel.rightLeg;
            case DefaultBipedBoneIdents.RIGHT_ARM_ARMOR_BONE_IDENT -> armorModel.rightArm;
            case DefaultBipedBoneIdents.LEFT_ARM_ARMOR_BONE_IDENT -> armorModel.leftArm;
            case DefaultBipedBoneIdents.BODY_ARMOR_BONE_IDENT -> armorModel.body;
            case DefaultBipedBoneIdents.HEAD_ARMOR_BONE_IDENT -> armorModel.head;
            default -> null;
        };
    }

    @Override
    protected void prepareArmorPositionAndScale(GeoBone bone, List<ModelPart.Cube> cubeList, ModelPart sourceLimb, PoseStack poseStack, boolean geoArmor, boolean modMatrixRot) {
        if (bone.getName().equals(GenericCustomArmorRenderer.leggingTorsoLayerBone)) {
            //IronsSpellbooks.LOGGER.debug("GeoHumanoidRenderer: attempting to prepare leggingTorsoLayer");
            super.prepareArmorPositionAndScale((GeoBone) this.modelProvider.getBone(DefaultBipedBoneIdents.BODY_ARMOR_BONE_IDENT), cubeList, sourceLimb, poseStack, false, modMatrixRot);
        } else {
            super.prepareArmorPositionAndScale(bone, cubeList, sourceLimb, poseStack, false, modMatrixRot);
        }
    }

    @Override
    protected EquipmentSlot getEquipmentSlotForArmorBone(String boneName, T currentEntity) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_FOOT_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.RIGHT_FOOT_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.LEFT_FOOT_ARMOR_BONE_2_IDENT,
                    DefaultBipedBoneIdents.RIGHT_FOOT_ARMOR_BONE_2_IDENT -> EquipmentSlot.FEET;
            case DefaultBipedBoneIdents.LEFT_LEG_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.RIGHT_LEG_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.LEFT_LEG_ARMOR_BONE_2_IDENT,
                    DefaultBipedBoneIdents.RIGHT_LEG_ARMOR_BONE_2_IDENT -> EquipmentSlot.LEGS;
            case DefaultBipedBoneIdents.RIGHT_ARM_ARMOR_BONE_IDENT ->
                    !currentEntity.isLeftHanded() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            case DefaultBipedBoneIdents.LEFT_ARM_ARMOR_BONE_IDENT ->
                    currentEntity.isLeftHanded() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            case DefaultBipedBoneIdents.BODY_ARMOR_BONE_IDENT -> EquipmentSlot.CHEST;
            case DefaultBipedBoneIdents.HEAD_ARMOR_BONE_IDENT -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    @Override
    protected ItemStack getArmorForBone(String boneName, T currentEntity) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_FOOT_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.RIGHT_FOOT_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.LEFT_FOOT_ARMOR_BONE_2_IDENT,
                    DefaultBipedBoneIdents.RIGHT_FOOT_ARMOR_BONE_2_IDENT ->
                    currentEntity.getItemBySlot(EquipmentSlot.FEET);
            case DefaultBipedBoneIdents.LEFT_LEG_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.RIGHT_LEG_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.LEFT_LEG_ARMOR_BONE_2_IDENT,
                    DefaultBipedBoneIdents.RIGHT_LEG_ARMOR_BONE_2_IDENT ->
                    currentEntity.getItemBySlot(EquipmentSlot.LEGS);
            case DefaultBipedBoneIdents.BODY_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.RIGHT_ARM_ARMOR_BONE_IDENT,
                    DefaultBipedBoneIdents.LEFT_ARM_ARMOR_BONE_IDENT ->
                    currentEntity.getItemBySlot(EquipmentSlot.CHEST);
            case DefaultBipedBoneIdents.HEAD_ARMOR_BONE_IDENT -> currentEntity.getItemBySlot(EquipmentSlot.HEAD);
            default -> null;
        };
    }

//    @Override
//    protected void setLimbBoneVisible(GeoArmorRenderer<? extends GeoArmorItem> armorRenderer, ModelPart limb, HumanoidModel<?> armorModel, EquipmentSlot slot) {
//        super.setLimbBoneVisible(armorRenderer, limb, armorModel, slot);
//        IBone gbBootL = armorRenderer.getGeoModelProvider().getBone(GenericCustomArmorRenderer.leggingTorsoLayerBone);
//        gbBootL.setHidden(true);
//        if (limb == armorModel.body) {
//            if (slot == EquipmentSlot.LEGS) {
//                gbBootL.setHidden(false);
//            }
//            return;
//        }
//    }


    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    @Override
    protected void handleArmorRenderingForBone(GeoBone bone, PoseStack stack, VertexConsumer buffer, int packedLight, int packedOverlay, ResourceLocation currentTexture) {
        super.handleArmorRenderingForBone(bone, stack, buffer, packedLight, packedOverlay, currentTexture);
        for (EquipmentSlot slot : SLOTS)
            if (currentEntityBeingRendered.getItemBySlot(slot).getItem() instanceof GeoArmorItem geoArmorItem) {
                if (GeoArmorRenderer.getRenderer(geoArmorItem.getClass(), this.currentEntityBeingRendered) instanceof GenericCustomArmorRenderer<?> armorRenderer) {

                }
                //HumanoidModel<?> armorModel = (HumanoidModel<?>) geoArmorRenderer;
            }

    }

    @Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, T entity) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT ->
                    entity.isLeftHanded() ? entity.getMainHandItem() : entity.getOffhandItem();
            case DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT ->
                    entity.isLeftHanded() ? entity.getOffhandItem() : entity.getMainHandItem();
            default -> null;
        };
    }

    @Override
    protected ItemTransforms.TransformType getCameraTransformForItemAtBone(ItemStack stack, String boneName) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT -> ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
            default -> ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
        };
    }

    @Nullable
    @Override
    protected BlockState getHeldBlockForBone(String boneName, T animatable) {
        return null;
    }

    @Override
    protected void preRenderItem(PoseStack poseStack, ItemStack itemStack, String boneName, T animatable, IBone bone) {
        var mainHandItem = animatable.getMainHandItem();
        var offHandItem = animatable.getOffhandItem();
        poseStack.translate(0, 0, -0.0625);
        poseStack.translate(0, -0.0625, 0);

        if (itemStack == mainHandItem) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90f));

            if (itemStack.getItem() instanceof ShieldItem)
                poseStack.translate(0, 0.125, -0.25);
        } else if (itemStack == offHandItem) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90f));

            if (itemStack.getItem() instanceof ShieldItem) {
                poseStack.translate(0, 0.125, 0.25);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
            }
        }
    }

//    @Override
//    protected void handleItemAndBlockBoneRendering(PoseStack poseStack, GeoBone bone, @Nullable ItemStack boneItem, @Nullable BlockState boneBlock, int packedLight, int packedOverlay) {
//        IronsSpellbooks.LOGGER.debug("{}",bone!=null?bone.getName():"null bone");
//        super.handleItemAndBlockBoneRendering(poseStack, bone, boneItem, boneBlock, packedLight, packedOverlay);
//    }

    @Override
    protected void preRenderBlock(PoseStack poseStack, BlockState state, String boneName, T animatable) {

    }

    @Override
    protected void postRenderItem(PoseStack poseStack, ItemStack stack, String boneName, T animatable, IBone bone) {

    }

    @Override
    protected void postRenderBlock(PoseStack poseStack, BlockState state, String boneName, T animatable) {

    }

    @Override
    public Color getRenderColor(T animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight) {
        return animatable.isInvisible() ? Color.ofRGBA(1f, 1f, 1f, .3f) : super.getRenderColor(animatable, partialTick, poseStack, bufferSource, buffer, packedLight);
    }

    @Override
    public RenderType getRenderType(T animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return animatable.isInvisible() ? RenderType.entityTranslucent(texture) : RenderType.entityCutout(texture);
    }
}