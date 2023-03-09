package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile, @Nullable ProfilePublicKey pProfilePublicKey) {
        super(pClientLevel, pGameProfile, pProfilePublicKey);
    }


    @Shadow
    public abstract boolean isUsingItem();

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    public boolean addSlowdownCondition(LocalPlayer instance) {
        return isUsingItem() || ClientMagicData.isCasting();
    }
}