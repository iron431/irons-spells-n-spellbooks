package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract boolean isFree(double pX, double pY, double pZ);

    /**
    Necessary to integrate summons into ally checks
    */
    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "HEAD"), cancellable = true)
    public void isAlliedTo(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Entity self = ((Entity) (Object) this);
        //IronsSpellbooks.LOGGER.debug("EntityMixin.isAlliedTo Check: {} allied to {}: {}", ((Entity) (Object) this).getName().getString(), entity.getName().getString(), flag);
        if (entity instanceof MagicSummon summon && summon.getSummoner() != null)
            cir.setReturnValue(self.isAlliedTo(summon.getSummoner()) || self.equals(summon.getSummoner()));

    }

    /**
    Necessary see all invisible mobs
    */
    @Inject(method = "isInvisibleTo", at = @At(value = "HEAD"), cancellable = true)
    public void isInvisibleTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (ItemRegistry.INVISIBILITY_RING.get().isEquippedBy(player)) {
            cir.setReturnValue(false);
        }
    }

    /**
     Necessary see color glowing mob outlines while we have the echolocation effect
     */
    @Inject(method = "getTeamColor", at = @At(value = "HEAD"), cancellable = true)
    public void changeGlowOutline(CallbackInfoReturnable<Integer> cir){
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(MobEffectRegistry.PLANAR_SIGHT.get())){
            cir.setReturnValue(0x6c42f5);
        }
    }

}
