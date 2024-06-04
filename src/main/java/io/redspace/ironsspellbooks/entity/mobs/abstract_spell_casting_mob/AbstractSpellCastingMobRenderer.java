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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
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

    protected boolean shouldWeaponBeSheathed(AbstractSpellCastingMob entity) {
        return entity.shouldSheathSword() && !entity.isAggressive();
    }

    protected boolean isBoneMainHand(AbstractSpellCastingMob entity, String boneName) {
        return entity.isLeftHanded() && boneName.equals(DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT) || !entity.isLeftHanded() && boneName.equals(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT);
    }

    @Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, AbstractSpellCastingMob entity) {
        if (isBoneMainHand(entity, boneName)) {
            if (animatable.isDrinkingPotion()) {
                return makePotion(entity);
            }
            if (shouldWeaponBeSheathed(entity) && entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof SwordItem) {
                return ItemStack.EMPTY;
            }
        }
        if (boneName.equals("torso")) {
            if (shouldWeaponBeSheathed(entity) && entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof SwordItem) {
                return entity.getItemBySlot(EquipmentSlot.MAINHAND);
            }
        }
        return super.getHeldItemForBone(boneName, entity);
    }

    @Override
    protected void preRenderItem(PoseStack poseStack, ItemStack itemStack, String boneName, AbstractSpellCastingMob animatable, IBone bone) {
        if (isBoneMainHand(animatable, boneName)) {
            if (animatable.isDrinkingPotion()) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90f));
            }
        }
        if (boneName.equals("torso")) {
            if (shouldWeaponBeSheathed(animatable)) {
                float hipOffset = animatable.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? .2f : .275f;
                poseStack.translate(animatable.isLeftHanded() ? hipOffset : -hipOffset, -.45, -.225);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-140f));
                poseStack.scale(.85f, .85f, .85f);
            }
        }
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
        SpellRenderingHelper.renderSpellHelper(ClientMagicData.getSyncedSpellData(animatable), animatable, poseStack, bufferSource, partialTick);
        poseStack.popPose();
    }


}