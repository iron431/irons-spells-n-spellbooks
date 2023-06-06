package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public class GlassBottleMixin {
    //TODO: delete me and go back to events
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        HitResult entityHit = Utils.raycastForEntity(level, player, (float) player.getEntityReach(), true);
        if (entityHit.getType() != HitResult.Type.MISS && ((EntityHitResult) entityHit).getEntity() instanceof Creeper creeper) {
            if (creeper.isPowered()) {
                //creeper.ignite();
                creeper.hurt(level.damageSources().generic(), 5);
                level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);

                ItemStack bottleStack = player.getItemInHand(hand);
                cir.setReturnValue(InteractionResultHolder.sidedSuccess(ItemUtils.createFilledResult(bottleStack, player, new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get())), level.isClientSide));
                //cir = new CallbackInfoReturnable<>("lightning_bottle_mixin", false, InteractionResultHolder.sidedSuccess(ItemUtils.createFilledResult(bottleStack, player, new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get())), level.isClientSide));
            }
        }
    }
}
