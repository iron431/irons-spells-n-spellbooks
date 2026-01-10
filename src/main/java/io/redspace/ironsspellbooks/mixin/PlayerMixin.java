package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.patreon.transmog.ITransmogPreview;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogClientHandler;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin implements ITransmogPreview {

    @Inject(method = "canEat", at = @At(value = "RETURN"), cancellable = true)
    void irons_spellbooks$canEatForGluttony(boolean pCanAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
        if (((Player) (Object) this).hasEffect(MobEffectRegistry.GLUTTONY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isModelPartShown", at = @At(value = "RETURN"), cancellable = true)
    void irons_spellbooks$hideJacketLayers(PlayerModelPart part, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            var self = (Player) (Object) this;
            boolean disable =
                    switch (part) {
                        case HAT -> TransmogClientHandler.disableOuterLayer(self, EquipmentSlot.HEAD);
                        case JACKET,
                             LEFT_SLEEVE,
                             RIGHT_SLEEVE -> TransmogClientHandler.disableOuterLayer(self, EquipmentSlot.CHEST);
                        case LEFT_PANTS_LEG,
                             RIGHT_PANTS_LEG ->
                                (TransmogClientHandler.disableOuterLayer(self, EquipmentSlot.LEGS) || TransmogClientHandler.disableOuterLayer(self, EquipmentSlot.FEET));
                        default -> false;
                    };
            if (disable) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getItemBySlot", at = @At(value = "RETURN"), cancellable = true)
    void irons_spellbooks$replaceTransmogStack(EquipmentSlot slot1, CallbackInfoReturnable<ItemStack> cir) {
        if (!slot1.isArmor()) {
            return;
        }
        if (TransmogClientHandler.hideForTransmog((Player) (Object) this, cir.getReturnValue())) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Unique
    boolean irons_spellbooks$transmogPreview;

    @Override
    public boolean irons_spellbooks$isTransmogPreview() {
        return irons_spellbooks$transmogPreview;
    }

    @Override
    public void irons_spellbooks$setTransmogPreview(boolean preview) {
        this.irons_spellbooks$transmogPreview = preview;
    }
}
