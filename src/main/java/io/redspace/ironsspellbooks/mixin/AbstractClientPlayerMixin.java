package io.redspace.ironsspellbooks.mixin;

import com.mojang.authlib.GameProfile;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import io.redspace.ironsspellbooks.item.armor.ArmorCapeProvider;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin{

    @Inject(method = "getCloakTextureLocation", at = @At(value = "HEAD"), cancellable = true)
    public void getCloakTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        var player = (Player) (Object)this;
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (itemstack.getItem() instanceof ArmorCapeProvider capeProvider && !player.hasEffect(MobEffectRegistry.ANGEL_WINGS.get())) {
            cir.setReturnValue(capeProvider.getCapeResourceLocation());
        }
    }

    @Inject(method = "isCapeLoaded", at = @At(value = "HEAD"), cancellable = true)
    public void isCapeLoaded(CallbackInfoReturnable<Boolean> cir) {
        var player = (Player) (Object)this;
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (itemstack.getItem() instanceof ArmorCapeProvider capeProvider) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void postInit(ClientLevel world, GameProfile profile, ProfilePublicKey publicKey, CallbackInfo ci) {
        var player = (Player) (Object)this;
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(AbstractSpell.ANIMATION_RESOURCE);
        if (animation != null) {
            animation.addModifierLast(new AdjustmentModifier((partName) -> {
                float rotationX = 0;
                float rotationY = 0;
                float rotationZ = 0;
                float offsetX = 0;
                float offsetY = 0;
                float offsetZ = 0;
                var pitch = player.getXRot();
                var yaw = player.yHeadRot - player.yBodyRot;
                pitch = (float) Math.toRadians(pitch);
                yaw = (float) Math.toRadians(yaw);
                switch (partName) {
//                        case "body" -> {
//                            rotationX = (-1F) * pitch;
//                        }
                    case "rightArm", "leftArm" -> {
                        rotationX = pitch;
                        rotationY = yaw;
                    }
                    case "head" -> {
                        //rotationX = pitch / 8;
                        //rotationY = (float) Math.toRadians(player.yHeadRot - player.yBodyRot);
                    }
                    default -> {
                        return Optional.empty();
                    }
                }
                return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(rotationX, rotationY, rotationZ), new Vec3f(offsetX, offsetY, offsetZ)));
            }));
            animation.addModifierLast(new MirrorModifier(){
                @Override
                public boolean isEnabled() {
                    return player.getUsedItemHand() == InteractionHand.OFF_HAND;
                }
            });
        }

    }

}
