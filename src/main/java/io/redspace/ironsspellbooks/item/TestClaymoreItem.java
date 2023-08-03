package io.redspace.ironsspellbooks.item;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.item.weapons.MagehunterItem;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.shadowed.eliotlash.mclib.math.functions.limit.Min;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.registries.ItemRegistry.TEST_CLAYMORE;

public class TestClaymoreItem extends MagehunterItem {
    public TestClaymoreItem() {
        super(SpellType.TELEPORT_SPELL, 4);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);

        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new ClaymoreRenderer<>(Minecraft.getInstance().getItemRenderer(),
                        Minecraft.getInstance().getEntityModels(),
                        TEST_CLAYMORE);
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                return false;
            }
        });
    }

    public static class ClaymoreRenderer<T extends Item> extends BlockEntityWithoutLevelRenderer {

        private final Supplier<T> me;
        private final ItemRenderer renderer;

        public ClaymoreRenderer(ItemRenderer renderDispatcher, EntityModelSet modelSet, Supplier<T> me) {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), modelSet);
            this.me = me;
            this.renderer = renderDispatcher;
        }


        @Override
        public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            BakedModel model;
            if (transformType == ItemTransforms.TransformType.GUI) {
                Lighting.setupForFlatItems();
                model = renderer.getItemModelShaper().getModelManager().getModel(new ResourceLocation(IronsSpellbooks.MODID, "item/claymore_gui") );
                //model = renderer.getItemModelShaper().getItemModel(Items.DIAMOND_SWORD);
                renderer.render(itemStack, transformType, false, poseStack, bufferIn, combinedLightIn, combinedOverlayIn, model);
                //renderGuiItem(itemStack, 50, 50, model);
            } else {
                model = renderer.getItemModelShaper().getModelManager().getModel(new ResourceLocation(IronsSpellbooks.MODID, "item/claymore_normal") );
                //model = renderer.getItemModelShaper().getItemModel(Items.NETHERITE_SWORD);
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    boolean leftHand = transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
                    renderer.render(itemStack, transformType, leftHand, poseStack, bufferIn,/* player.level, */combinedLightIn, combinedOverlayIn, model/*, 0*/);
                }
            }
            //RenderSystem.applyModelViewMatrix();
            //model = model.applyTransform(transformType, poseStack, transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
            //renderer.render(itemStack, transformType, false, poseStack, bufferIn, combinedLightIn, combinedOverlayIn, model);
            //super.renderByItem(itemStack,transformType,poseStack,bufferIn,combinedLightIn,combinedOverlayIn);
            poseStack.popPose();
        }

        protected void renderGuiItem(ItemStack pStack, int pX, int pY, BakedModel pBakedModel) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate((double) pX, (double) pY, (double) (100.0F));
            posestack.translate(8.0D, 8.0D, 0.0D);
            posestack.scale(1.0F, -1.0F, 1.0F);
            posestack.scale(16.0F, 16.0F, 16.0F);
            RenderSystem.applyModelViewMatrix();
            PoseStack posestack1 = new PoseStack();
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            boolean flag = !pBakedModel.usesBlockLight();
            if (flag) {
                Lighting.setupForFlatItems();
            }

            renderer.render(pStack, ItemTransforms.TransformType.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, pBakedModel);
            multibuffersource$buffersource.endBatch();
            RenderSystem.enableDepthTest();
            if (flag) {
                Lighting.setupFor3DItems();
            }

            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

}
