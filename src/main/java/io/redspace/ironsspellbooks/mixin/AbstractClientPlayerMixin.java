package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.item.ArmorCapeProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {

    public AbstractClientPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile, @Nullable ProfilePublicKey pProfilePublicKey) {
        super(pLevel, pPos, pYRot, pGameProfile, pProfilePublicKey);
    }

    @Inject(method = "getCloakTextureLocation", at = @At(value = "HEAD"), cancellable = true)
    public void getCloakTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
        if(itemstack.getItem() instanceof ArmorCapeProvider capeProvider){
            cir.setReturnValue(capeProvider.getCapeResourceLocation());
        }
    }

    @Inject(method = "isCapeLoaded", at = @At(value = "HEAD"), cancellable = true)
    public void isCapeLoaded(CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
        if(itemstack.getItem() instanceof ArmorCapeProvider capeProvider){
            cir.setReturnValue(true);
        }
    }

}
