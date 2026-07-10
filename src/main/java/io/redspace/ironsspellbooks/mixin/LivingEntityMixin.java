package io.redspace.ironsspellbooks.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "onEffectRemoved", at = @At(value = "HEAD"))
    public void irons_spellbooks$onEffectRemoved(MobEffectInstance effectInstance, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide) {
            if (effectInstance.getEffect().value() instanceof IMobEffectEndCallback mobEffect) {
                mobEffect.onEffectRemoved(self, effectInstance.getAmplifier());
            }
            if (effectInstance.getEffect().value() instanceof ISyncedMobEffect && self.level.getChunkSource() instanceof ServerChunkCache serverChunk) {
                serverChunk.broadcast(self, new ClientboundRemoveMobEffectPacket(self.getId(), effectInstance.getEffect()));
            }
        }
    }

    @Inject(method = "onEffectUpdated", at = @At(value = "HEAD"))
    public void irons_spellbooks$onEffectUpdated(MobEffectInstance effectInstance, boolean forced, Entity entity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide) {
            if (effectInstance.getEffect().value() instanceof ISyncedMobEffect && self.level.getChunkSource() instanceof ServerChunkCache serverChunk) {
                serverChunk.broadcast(self, new ClientboundUpdateMobEffectPacket(self.getId(), effectInstance, false));
            }
        }
    }

    @Inject(method = "onEffectAdded", at = @At(value = "HEAD"))
    public void irons_spellbooks$onEffectAdded(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide) {
            if (effectInstance.getEffect().value() instanceof ISyncedMobEffect && self.level.getChunkSource() instanceof ServerChunkCache serverChunk) {
                serverChunk.broadcast(self, new ClientboundUpdateMobEffectPacket(self.getId(), effectInstance, false));
            }
        }
    }

    @Inject(method = "updateInvisibilityStatus", at = @At(value = "TAIL"))
    public void irons_spellbooks$updateInvisibilityStatus(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY))
            self.setInvisible(true);
    }

    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void irons_spellbooks$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide() && self.hasEffect(MobEffectRegistry.GUIDING_BOLT)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    public void irons_spellbooks$changeSummonHurtCredit(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        IMagicSummon fromSummon = damageSource.getDirectEntity() instanceof IMagicSummon summon ? summon : damageSource.getEntity() instanceof IMagicSummon summon ? summon : null;
        if (fromSummon instanceof LivingEntity livingSummon) {
            ((LivingEntity) (Object) this).setLastHurtByMob(livingSummon);
        }
    }
}