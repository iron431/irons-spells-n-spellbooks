package io.redspace.ironsspellbooks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SpinAttackEffectLayer.class)
public class SpinAttackEffectLayerMixin {
    private static final ResourceLocation FIRE_TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/fire_riptide.png");

    @ModifyVariable(method = "render", at = @At("STORE"))
    public VertexConsumer selectSpinAttackTexture(VertexConsumer original, PoseStack poseStack, MultiBufferSource buffer, int p_117528_, LivingEntity livingEntity, float f1, float f2, float f3, float f4, float f5, float f6) {
        switch (ClientMagicData.getSyncedSpellData(livingEntity).getSpinAttackType()) {
            case FIRE:
                return buffer.getBuffer(RenderType.entityCutoutNoCull(FIRE_TEXTURE));
            default:
                return original;
        }
    }

//    @ModifyArg(
//            method = "render",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"),
//            index = 2
//    )
//    int modifyLight(int originalLight) {
//                return LightTexture.FULL_BRIGHT;
//    }
}