package io.redspace.ironsspellbooks.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Attributes.class)
public class AttributesMixin {
    /**
     * In order to utilize the player's attribute values in tooltips, we need additional attributes to be set as syncable.
     */
    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void makeSynced(CallbackInfo ci) {
        Attributes.ATTACK_DAMAGE.setSyncable(true);
    }
}
