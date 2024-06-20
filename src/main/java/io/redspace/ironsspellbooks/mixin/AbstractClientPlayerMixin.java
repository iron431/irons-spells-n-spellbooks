package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.item.armor.ArmorCapeProvider;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @Inject(method = "getCloakTextureLocation", at = @At(value = "HEAD"), cancellable = true)
    public void getCloakTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        var player = (Player) (Object) this;
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (itemstack.getItem() instanceof ArmorCapeProvider capeProvider && !player.hasEffect(MobEffectRegistry.ANGEL_WINGS.get())) {
            cir.setReturnValue(capeProvider.getCapeResourceLocation());
        }
    }

    @Inject(method = "isCapeLoaded", at = @At(value = "HEAD"), cancellable = true)
    public void isCapeLoaded(CallbackInfoReturnable<Boolean> cir) {
        var player = (Player) (Object) this;
        if (ClientMagicData.getSyncedSpellData(player).hasEffect(SyncedSpellData.ANGEL_WINGS)) {
            cir.setReturnValue(false);
            return;
        }
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (itemstack.getItem() instanceof ArmorCapeProvider capeProvider) {
            cir.setReturnValue(true);
        }
    }
}
