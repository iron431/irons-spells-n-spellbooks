package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {


    /**
    Necessary to render additional effects based on the entity while casting when they might otherwise cull themselves
     */
    @Inject(method = "shouldRender", at = @At(value = "HEAD"), cancellable = true)
    public void renderRayOverride(T pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ, CallbackInfoReturnable<Boolean> cir) {
        //TODO: this is constantly creating new spell data.. Should optimize this with an additional check?
        if ((pLivingEntity instanceof Player || pLivingEntity instanceof IMagicEntity) && ClientMagicData.getSyncedSpellData((LivingEntity) pLivingEntity).isCasting())
            cir.setReturnValue(true);
    }
}
