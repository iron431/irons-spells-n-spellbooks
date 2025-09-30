package io.redspace.ironsspellbooks.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.attribute.IMagicAttribute;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.item.armor.IArmorCapeProvider;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements MagicData.IExtendedEntity {

    @Unique
    MagicData irons_spellbooks$magicData = null;
    @Unique
    IArmorCapeProvider.CapeData irons_spellbooks$capeData = null;

    @Override
    public IArmorCapeProvider.CapeData irons_spellbooks$getCapData() {
        if (irons_spellbooks$capeData == null) {
            irons_spellbooks$capeData = new IArmorCapeProvider.CapeData();
        }
        return irons_spellbooks$capeData;
    }

    @Override
    public MagicData irons_spellbooks$getMagicData() {
        if (irons_spellbooks$magicData == null) {
            if ((Object) this instanceof Player player) {
                if (player instanceof ServerPlayer serverplayer) {
                    irons_spellbooks$magicData = new MagicData(serverplayer);
                } else {
                    irons_spellbooks$magicData = new MagicData();
                }
            } else {
                irons_spellbooks$magicData = new MagicData(true);
            }
        }
        assert irons_spellbooks$magicData != null;
        return irons_spellbooks$magicData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void irons_spellbooks$saveDataAttachment(CompoundTag pCompound, CallbackInfo ci) {
        if (irons_spellbooks$magicData == null) {
            return;
        }
        CompoundTag tag = new CompoundTag();
        irons_spellbooks$magicData.saveNBTData(tag, ((Entity) (Object) this).level.registryAccess());
        pCompound.put("irons_spellbooks:magic_data", tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void irons_spellbooks$readDataAttachment(CompoundTag pCompound, CallbackInfo ci) {
        if (!pCompound.contains("irons_spellbooks:magic_data")) {
            return;
        }
        irons_spellbooks$getMagicData(); // initialize
        CompoundTag tag = pCompound.getCompound("irons_spellbooks:magic_data");
        irons_spellbooks$magicData.loadNBTData(tag, ((Entity) (Object) this).level.registryAccess());
    }

    @Inject(method = "onEffectRemoved", at = @At(value = "HEAD"))
    public void irons_spellbooks$onEffectRemoved(MobEffectInstance effectInstance, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide) {
            if (effectInstance.getEffect() instanceof IMobEffectEndCallback mobEffect) {
                mobEffect.onEffectRemoved(self, effectInstance.getAmplifier());
            }
            if (effectInstance.getEffect() instanceof ISyncedMobEffect && self.level.getChunkSource() instanceof ServerChunkCache serverChunk) {
                serverChunk.broadcast(self, new ClientboundRemoveMobEffectPacket(self.getId(), effectInstance.getEffect()));
            }
        }
    }

    @Inject(method = "onEffectUpdated", at = @At(value = "HEAD"))
    public void irons_spellbooks$onEffectUpdated(MobEffectInstance effectInstance, boolean forced, Entity entity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide) {
            if (effectInstance.getEffect() instanceof ISyncedMobEffect && self.level.getChunkSource() instanceof ServerChunkCache serverChunk) {
                serverChunk.broadcast(self, new ClientboundUpdateMobEffectPacket(self.getId(), effectInstance));
            }
        }
    }

    @Inject(method = "onEffectAdded", at = @At(value = "HEAD"))
    public void irons_spellbooks$onEffectAdded(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide) {
            if (effectInstance.getEffect() instanceof ISyncedMobEffect && self.level.getChunkSource() instanceof ServerChunkCache serverChunk) {
                serverChunk.broadcast(self, new ClientboundUpdateMobEffectPacket(self.getId(), effectInstance));
            }
        }
    }

    @Inject(method = "updateInvisibilityStatus", at = @At(value = "TAIL"))
    public void irons_spellbooks$updateInvisibilityStatus(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get()))
            self.setInvisible(true);
    }

    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void irons_spellbooks$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide() && self.hasEffect(MobEffectRegistry.GUIDING_BOLT.get())) {
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

    @Shadow
    abstract ItemStack getLastHandItem(EquipmentSlot pSlot);

    @Unique
    private static final List<EquipmentSlot> handSlots = List.of(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);

    @Unique
    private static Multimap<Attribute, AttributeModifier> filterApplicableAttributes(Multimap<Attribute, AttributeModifier> attributeModifierMap) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        for (Attribute attribute : attributeModifierMap.keySet()) {
            Predicate<Attribute> predicate = ServerConfigs.APPLY_ALL_MULTIHAND_ATTRIBUTES.get() ? allNonBaseAttackAttributes : onlyIronAttributes;
            if (predicate.test(attribute)) {
                map.putAll(attribute, attributeModifierMap.get(attribute));
            }
        }
        return map;
    }

    @Unique
    private static final Predicate<Attribute> allNonBaseAttackAttributes = (attribute) -> !(attribute == ForgeMod.ENTITY_REACH.get() || attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED || attribute == Attributes.ATTACK_KNOCKBACK);
    @Unique
    private static final Predicate<Attribute> onlyIronAttributes = (attribute) -> attribute instanceof IMagicAttribute;

}