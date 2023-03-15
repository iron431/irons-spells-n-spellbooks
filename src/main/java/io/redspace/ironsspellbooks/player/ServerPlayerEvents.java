package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.effect.AbyssalShroudEffect;
import io.redspace.ironsspellbooks.effect.EvasionEffect;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber()
public class ServerPlayerEvents {

    @SubscribeEvent()
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {

        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()
                    && (event.getSlot().getIndex() == 0 || event.getSlot().getIndex() == 1)
                    && (event.getFrom().getItem() instanceof SpellBook || event.getFrom().getItem() instanceof Scroll || event.getFrom().getItem() instanceof SwordItem)) {
                Utils.serverSideCancelCast(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        IronsSpellbooks.LOGGER.debug("onPlayerOpenContainer {} {}", event.getEntity().getName().getString(), event.getContainer().getType());

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                Utils.serverSideCancelCast(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void upgradeModifiers(ItemAttributeModifierEvent event) {
        var itemStack = event.getItemStack();

        //Placeholders for logic
        var tempattribute = AttributeRegistry.FIRE_SPELL_POWER.get();
        var tempslot = EquipmentSlot.HEAD;
        var tempamount = .1d;

        //we want to store slot, attribute, and levels applied.
        //storing the slot seems good for performance as we dont want to constantly have to search for it
        //store the attribute... no shit
        //amount per level is fixed, so we can calculate our modifier using only the levels applied

//        if (itemStack.is(ItemRegistry.PYROMANCER_HELMET.get()) && event.getSlotType() == tempslot) {
//            var originalModifiers = event.removeAttribute(tempattribute);
//            double amount = .1 + UpgradeUtils.getValueByOperation(originalModifiers, AttributeModifier.Operation.MULTIPLY_BASE);
//            event.addModifier(tempattribute, new AttributeModifier(UpgradeUtils.UUIDForSlot(tempslot), "upgrade", amount, AttributeModifier.Operation.MULTIPLY_BASE));
//            originalModifiers.forEach((modifier) -> {
//                if (modifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE)
//                    event.addModifier(tempattribute, modifier);
//            });
//        }
        if (itemStack.is(ItemRegistry.PYROMANCER_HELMET.get()) && event.getSlotType() == tempslot) {
            double baseAmount = UpgradeUtils.collectAndRemovePreexistingAttribute(event, tempattribute, AttributeModifier.Operation.MULTIPLY_BASE);
            event.addModifier(tempattribute, new AttributeModifier(UpgradeUtils.UUIDForSlot(tempslot), "upgrade", baseAmount + tempamount, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    @SubscribeEvent
    public static void onStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && event.getTarget() instanceof ServerPlayer targetPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).getSyncedData().syncToPlayer(targetPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            IronsSpellbooks.LOGGER.debug("onPlayerLoggedIn syncing cooldowns to {}", serverPlayer.getName().getString());
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            playerMagicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
            playerMagicData.getSyncedData().syncToPlayer(serverPlayer);
        }
    }

//This causes an issue with saving the PlayerMagicData to nbt. you can't save it if you clear it.
//    @SubscribeEvent
//    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
//        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
//            PlayerMagicData.getPlayerMagicData(serverPlayer).resetCastingState();
//        }
//    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        IronsSpellbooks.LOGGER.debug("onPlayerCloned: {}", event.getEntity().getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        IronsSpellbooks.LOGGER.debug("PlayerChangedDimension: {}", event.getEntity().getName().getString());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            IronsSpellbooks.LOGGER.debug("onPlayerLoggedIn syncing cooldowns to {}", serverPlayer.getName().getString());
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            playerMagicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
            playerMagicData.getSyncedData().syncToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            playerMagicData.resetCastingState();
            playerMagicData.getSyncedData().doSync();
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        var livingEntity = event.getEntity();
        //irons_spellbooks.LOGGER.debug("onLivingAttack.1: {}", livingEntity);

        if (!(livingEntity instanceof ServerPlayer) && !(livingEntity instanceof AbstractSpellCastingMob)) {
            return;
        }

        //irons_spellbooks.LOGGER.debug("onLivingAttack.2: {}", livingEntity);

        var playerMagicData = PlayerMagicData.getPlayerMagicData(livingEntity);
        if (playerMagicData.getSyncedData().hasEffect(SyncedSpellData.EVASION)) {
            if (EvasionEffect.doEffect(livingEntity, event.getSource())) {
                event.setCanceled(true);
            }
        }else if (playerMagicData.getSyncedData().hasEffect(SyncedSpellData.ABYSSAL_SHROUD)) {
            if (AbyssalShroudEffect.doEffect(livingEntity, event.getSource())) {
                event.setCanceled(true);
            }
        }
    }
//
//    @SubscribeEvent
//    public static void onMobTarget(LivingChangeTargetEvent event) {
//        var newTarget = event.getNewTarget();
//        var oldTarget = event.getOriginalTarget();
//        if (newTarget == null || oldTarget == null)
//            return;
//
//        if (newTarget.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get()))
//            event.setNewTarget(oldTarget);
//    }
//    @SubscribeEvent
//    public static void onPlayerHurt(LivingHurtEvent event) {
//        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
//            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
//            if (playerMagicData.getSyncedData().getHasEvasion()) {
//                if (EvasionEffect.doEffect(serverPlayer, event.getSource())) {
//                    event.setCanceled(true);
//                }
//            }
//        }
//    }

    @SubscribeEvent
    public static void onLivingTakeDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.getSyncedData().hasEffect(SyncedSpellData.HEARTSTOP)) {
                playerMagicData.getSyncedData().addHeartstopDamage(event.getAmount());
                IronsSpellbooks.LOGGER.debug("Accumulated damage: {}", playerMagicData.getSyncedData().getHeartstopAccumulatedDamage());
                event.setCanceled(true);
                return;
            }

            if (playerMagicData.isCasting() &&
                    SpellType.values()[playerMagicData.getCastingSpellId()].getCastType() == CastType.LONG &&
                    event.getSource() != DamageSource.ON_FIRE &&
                    event.getSource() != DamageSource.WITHER) {
                Utils.serverSideCancelCast(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityMountEvent(EntityMountEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                Utils.serverSideCancelCast(serverPlayer);
            }
        }
    }
}
