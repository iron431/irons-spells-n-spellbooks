package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)

public class ClientEntityMixin {

    /**
     * Necessary see color glowing mob outlines while we have the echolocation effect
     */
    @Inject(method = "getTeamColor", at = @At(value = "HEAD"), cancellable = true)
    public void changeGlowOutline(CallbackInfoReturnable<Integer> cir) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(MobEffectRegistry.PLANAR_SIGHT.get())) {
            cir.setReturnValue(0x6c42f5);
        } else if ((Entity) (Object) this instanceof ItemEntity item) {
            if (item.getItem().is(ItemRegistry.DRAGONSKIN.get())) {
                cir.setReturnValue(0xd21cff);
            }
            if (item.getItem().is(ItemRegistry.LIGHTNING_ROD_STAFF.get())) {
                cir.setReturnValue(0x55FFFF);
            }
        }
    }
}
