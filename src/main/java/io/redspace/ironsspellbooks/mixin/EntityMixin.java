package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    /*
    Necessary to integrate summons into ally checks
    */
    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "HEAD"), cancellable = true)
    public void isAlliedTo(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Entity self = ((Entity) (Object) this);
        //IronsSpellbooks.LOGGER.debug("EntityMixin.isAlliedTo Check: {} allied to {}: {}", ((Entity) (Object) this).getName().getString(), entity.getName().getString(), flag);
        if (entity instanceof MagicSummon summon && summon.getSummoner() != null)
            cir.setReturnValue(self.isAlliedTo(summon.getSummoner()) || self.equals(summon.getSummoner()));

    }


}
