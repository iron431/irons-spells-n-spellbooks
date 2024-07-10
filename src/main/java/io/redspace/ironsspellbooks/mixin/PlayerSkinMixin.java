package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.item.armor.ArmorCapeProvider;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkin.class)
public class PlayerSkinMixin {

    @Inject(method = "capeTexture", at = @At(value = "HEAD"), cancellable = true)
    public void getCloakTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        var player = (Player) (Object) this;
        if (ClientMagicData.getSyncedSpellData(player).hasEffect(SyncedSpellData.ANGEL_WINGS)) {
            cir.setReturnValue(null);
            return;
        }
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (itemstack.getItem() instanceof ArmorCapeProvider capeProvider && !player.hasEffect(MobEffectRegistry.ANGEL_WINGS)) {
            cir.setReturnValue(capeProvider.getCapeResourceLocation());
        }
    }
}
