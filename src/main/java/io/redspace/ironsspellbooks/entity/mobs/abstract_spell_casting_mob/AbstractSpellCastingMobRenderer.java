package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.HumanoidRenderer;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.render.*;
import io.redspace.ironsspellbooks.util.DefaultBipedBoneIdents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.CHARGE_TEXTURE;
import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.EVASION_TEXTURE;

public abstract class AbstractSpellCastingMobRenderer extends HumanoidRenderer<AbstractSpellCastingMob> {
    private ResourceLocation textureResource;

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager, AbstractSpellCastingMobModel model) {
        super(renderManager, model);
        this.shadowRadius = 0.5f;
        //this.addLayer(new GeoEvasionLayer(this));
        addRenderLayer(new EnergySwirlLayer.Geo(this, EVASION_TEXTURE, SyncedSpellData.EVASION));
        addRenderLayer(new EnergySwirlLayer.Geo(this, CHARGE_TEXTURE, SyncedSpellData.CHARGED));
        addRenderLayer(new ChargeSpellLayer.Geo(this));
        addRenderLayer(new GlowingEyesLayer.Geo(this));
        addRenderLayer(new SpellTargetingLayer.Geo(this));
        addRenderLayer(new GeoSpinAttackLayer(this));
    }

    public static ItemStack makePotion(AbstractSpellCastingMob entity) {
        ItemStack healthPotion = new ItemStack(Items.POTION);
        return PotionUtils.setPotion(healthPotion, entity.isInvertedHealAndHarm() ? Potions.HARMING : Potions.HEALING);
    }

    @Override
    public void render(AbstractSpellCastingMob entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        //poseStack.pushPose();
        //poseStack.mulPose(Axis.YP.rotationDegrees(90));
        SpellRenderingHelper.renderSpellHelper(ClientMagicData.getSyncedSpellData(animatable), animatable, poseStack, bufferSource, partialTick);
        //poseStack.popPose();

    }

    @Override
    public Color getRenderColor(AbstractSpellCastingMob animatable, float partialTick, int packedLight) {
        return animatable.isInvisible() ? Color.ofRGBA(1f, 1f, 1f, .3f) : super.getRenderColor(animatable, partialTick, packedLight);
    }

    @Override
    public RenderType getRenderType(AbstractSpellCastingMob animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return animatable.isInvisible() ? RenderType.entityTranslucent(texture) : super.getRenderType(animatable, texture, bufferSource, partialTick);
    }
}