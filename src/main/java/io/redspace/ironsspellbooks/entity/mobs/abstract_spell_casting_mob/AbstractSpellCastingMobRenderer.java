package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.render.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.CHARGE_TEXTURE;
import static io.redspace.ironsspellbooks.render.EnergySwirlLayer.EVASION_TEXTURE;

public abstract class AbstractSpellCastingMobRenderer extends GeoHumanoidRenderer<AbstractSpellCastingMob> {
    private ResourceLocation textureResource;

    public AbstractSpellCastingMobRenderer(EntityRendererProvider.Context renderManager, AbstractSpellCastingMobModel model) {
        super(renderManager, model);
        this.shadowRadius = 0.5f;
        //this.addLayer(new GeoEvasionLayer(this));
        this.addLayer(new EnergySwirlLayer.Geo(this, EVASION_TEXTURE, SyncedSpellData.EVASION));
        this.addLayer(new EnergySwirlLayer.Geo(this, CHARGE_TEXTURE, SyncedSpellData.CHARGED));
        this.addLayer(new ChargeSpellLayer.Geo(this));
        this.addLayer(new GlowingEyesLayer.Geo(this));
        this.addLayer(new SpellTargetingLayer.Geo(this));
        this.addLayer(new GeoSpinAttackLayer(this));
    }

    @Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, AbstractSpellCastingMob entity) {
        if (animatable.isDrinkingPotion()) {
            if (animatable.isLeftHanded() && boneName.equals(DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT) || !animatable.isLeftHanded() && boneName.equals(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT))
                return makePotion(entity);

        }
        return super.getHeldItemForBone(boneName, entity);
    }

    @Override
    protected void preRenderItem(PoseStack poseStack, ItemStack itemStack, String boneName, AbstractSpellCastingMob animatable, IBone bone) {
        if (currentEntityBeingRendered.isDrinkingPotion())
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90f));
        super.preRenderItem(poseStack, itemStack, boneName, animatable, bone);
    }

    private ItemStack makePotion(AbstractSpellCastingMob entity) {
        ItemStack healthPotion = new ItemStack(Items.POTION);
        return PotionUtils.setPotion(healthPotion, entity.isInvertedHealAndHarm() ? Potions.HARMING : Potions.HEALING);
    }

    @Override
    public void render(GeoModel model, AbstractSpellCastingMob animatable, float partialTick, RenderType type, PoseStack poseStack, MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.render(model, animatable, partialTick, type, poseStack, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
        SpellRenderingHelper.renderSpellHelper( ClientMagicData.getSyncedSpellData(animatable), animatable, poseStack, bufferSource, partialTick);
        poseStack.popPose();
    }
}