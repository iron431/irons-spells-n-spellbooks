package io.redspace.ironsspellbooks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SpinAttackEffectLayer.class)
public class SpinAttackEffectLayerMixin {

    @Unique
    private static boolean irons_spellbooks$fullbright = false;

    @ModifyVariable(method = "render", at = @At("STORE"))
    public VertexConsumer selectSpinAttackTexture(VertexConsumer original, PoseStack poseStack, MultiBufferSource buffer, int p_117528_, LivingEntity livingEntity, float f1, float f2, float f3, float f4, float f5, float f6) {
        irons_spellbooks$fullbright = false;
        SpinAttackType type = ClientMagicData.getSyncedSpellData(livingEntity).getSpinAttackType();
        if(type.textureId() == SpinAttackType.RIPTIDE.textureId()){
            return original;
        }
        irons_spellbooks$fullbright = type.fullbright();
        return buffer.getBuffer(RenderType.entityCutoutNoCull(type.textureId()));
    }

    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"),
            index = 2
    )
    int modifyLight(int originalLight) {
        return irons_spellbooks$fullbright ? LightTexture.FULL_BRIGHT : originalLight;
    }
}