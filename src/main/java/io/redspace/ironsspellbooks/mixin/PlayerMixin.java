package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.item.armor.IDisableHat;
import io.redspace.ironsspellbooks.item.armor.IDisableJacket;
import io.redspace.ironsspellbooks.patreon.transmog.TransmogClientHandler;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "canEat", at = @At(value = "RETURN"), cancellable = true)
    void canEatForGluttony(boolean pCanAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
        if (((Player) (Object) this).hasEffect(MobEffectRegistry.GLUTTONY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isModelPartShown", at = @At(value = "RETURN"), cancellable = true)
    void irons_spellbooks$hideJacketLayers(PlayerModelPart part, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            var self = (Player) (Object) this;
            switch (part) {
                case PlayerModelPart.HAT:
                    cir.setReturnValue(!(self.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof IDisableHat));
                    break;
                case JACKET:
                case LEFT_SLEEVE:
                case RIGHT_SLEEVE:
                    if (self.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IDisableJacket chestplate && chestplate.disableForSlot(EquipmentSlot.CHEST)) {
                        cir.setReturnValue(false);
                    }
                    break;
                case LEFT_PANTS_LEG:
                case RIGHT_PANTS_LEG:
                    if ((self.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof IDisableJacket leggings && leggings.disableForSlot(EquipmentSlot.LEGS))
                            || (self.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof IDisableJacket boots && boots.disableForSlot(EquipmentSlot.FEET))) {
                        cir.setReturnValue(false);
                    }
                    break;
            }
        }
    }

    @Inject(method = "getItemBySlot", at = @At(value = "RETURN"), cancellable = true)
    void replaceTransmogStack(EquipmentSlot slot1, CallbackInfoReturnable<ItemStack> cir) {
        if(!slot1.isArmor()){
            return;
        }
        Optional<ItemStack> replacement = TransmogClientHandler.handleTransmogReplacement((Player) (Object) this, cir.getReturnValue());
        if (replacement.isPresent()) {
            cir.setReturnValue(replacement.get());
        }
    }
}
