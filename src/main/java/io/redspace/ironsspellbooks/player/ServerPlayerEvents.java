package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.SpellTeleportEvent;
import io.redspace.ironsspellbooks.api.item.CastingImplementData;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.block.BloodCauldronBlock;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.datagen.DamageTypeTagGenerator;
import io.redspace.ironsspellbooks.effect.*;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.ICritablePartEntity;
import io.redspace.ironsspellbooks.entity.spells.ice_tomb.IceTombEntity;
import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.network.EquipmentChangedPacket;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@EventBusSubscriber
public class ServerPlayerEvents {

    //    @SubscribeEvent
//    public static void onPlayerAttack(AttackEntityEvent event) {
//        TODO: this only gets called when the player successfully hits something. we want it to cancel if they even try.
//              granted, the input even should be cancelled already, but better combat skips that due to custom weapon handling.
//        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
//            if (PlayerMagicData.getPlayerMagicData(serverPlayer).isCasting()) {
//                Utils.serverSideCancelCast(serverPlayer);
//            }
//        }
//    }

    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        var player = event.getEntity();
        if (player.level.isClientSide) {
            MinecraftInstanceHelper.ifPlayerPresent(localPlayer -> {
                if (ClientMagicData.isCasting() && player.getUUID().equals(localPlayer.getUUID())) {
                    event.setCanceled(true);
                }
            });
        } else {
            var magicData = MagicData.getPlayerMagicData(player);
            if (magicData.isCasting() && event.getItemStack() != magicData.getPlayerCastingItem()) {
                event.setCanceled(true);
            }
        }
        if (event.isCanceled()) {
            return;
        }

        var level = player.level;
        var hand = event.getHand();
        ItemStack itemStack = player.getItemInHand(hand);
        if (CastingImplementData.has(itemStack) && CastingImplementData.get(itemStack)) {
            SpellSelectionManager spellSelectionManager = new SpellSelectionManager(player);
            SpellSelectionManager.SelectionOption selectionOption = spellSelectionManager.getSelection();
            if (selectionOption == null || selectionOption.spellData.equals(SpellData.EMPTY)) {
                //IronsSpellbooks.LOGGER.debug("CastingItem.Use.1 {} {}", level.isClientSide, hand);
                return;
            }
            SpellData spellData = selectionOption.spellData;
            int spellLevel = spellData.getSpell().getLevelFor(spellData.getLevel(), player);
            if (level.isClientSide()) {
                if (ClientMagicData.isCasting()) {
                    //IronsSpellbooks.LOGGER.debug("CastingItem.Use.2 {} {}", level.isClientSide, hand);
                    event.setCancellationResult(InteractionResult.CONSUME);
                } else if (ClientMagicData.getPlayerMana() < spellData.getSpell().getManaCost(spellLevel)
                        || ClientMagicData.getCooldowns().isOnCooldown(spellData.getSpell())
                        || !ClientMagicData.getSyncedSpellData(player).isSpellLearned(spellData.getSpell())) {
                    //IronsSpellbooks.LOGGER.debug("CastingItem.Use.3 {} {}", level.isClientSide, hand);
                    return;
                } else {
                    //IronsSpellbooks.LOGGER.debug("CastingItem.Use.4 {} {}", level.isClientSide, hand);
                    event.setCancellationResult(InteractionResult.CONSUME);
                }
            }

            var castingSlot = hand.ordinal() == 0 ? SpellSelectionManager.MAINHAND : SpellSelectionManager.OFFHAND;

            if (spellData.getSpell().attemptInitiateCast(itemStack, spellLevel, level, player, selectionOption.getCastSource(), true, castingSlot)) {
                event.setCancellationResult(InteractionResult.CONSUME);
            } else {
                //IronsSpellbooks.LOGGER.debug("CastingItem.Use.6 {} {}", level.isClientSide, hand);
                event.setCancellationResult(InteractionResult.FAIL);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerDropItem(ItemTossEvent event) {
        var itemStack = event.getEntity().getItem();
        if (itemStack.getItem() instanceof Scroll) {
            var magicData = MagicData.getPlayerMagicData(event.getPlayer());
            if (magicData.isCasting() && magicData.getCastSource() == CastSource.SCROLL) {
                if (magicData.getCastType() == CastType.CONTINUOUS) {
                    itemStack.shrink(1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelLoaded(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
            IronsDataStorage.init(serverLevel.getDataStorage());
        }
    }

    @SubscribeEvent
    public static void onServerStoppedEvent(ServerStoppedEvent event) {
        IronsSpellbooks.MCS = null;
        IronsSpellbooks.OVERWORLD = null;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        IronsSpellbooks.MCS = event.getServer();
        IronsSpellbooks.OVERWORLD = IronsSpellbooks.MCS.overworld();
    }

    @SubscribeEvent
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);

            if (playerMagicData.isCasting() && (event.getFrom().getItem() instanceof CastingItem || event.getTo().getItem() instanceof CastingItem)) {
                Utils.serverSideCancelCast(serverPlayer);
                PacketDistributor.sendToPlayer(serverPlayer, new EquipmentChangedPacket());
                return;
            }

            var isFromSpellContainer = ISpellContainer.isSpellContainer(event.getFrom());
            if (isFromSpellContainer &&
                    ISpellContainer.get(event.getFrom()).getIndexForSpell(playerMagicData.getCastingSpell().getSpell()) >= 0 &&
                    !Utils.isSameItemSameComponentsIgnoreDurability(event.getFrom(), event.getTo())) {
                if (playerMagicData.isCasting()) {
                    Utils.serverSideCancelCast(serverPlayer);
                }
                PacketDistributor.sendToPlayer(serverPlayer, new EquipmentChangedPacket());
            } else if (isFromSpellContainer || ISpellContainer.isSpellContainer(event.getTo())) {
                PacketDistributor.sendToPlayer(serverPlayer, new EquipmentChangedPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onCurioChangeEvent(CurioChangeEvent event) {
        var entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer && (ISpellContainer.isSpellContainer(event.getFrom()) || ISpellContainer.isSpellContainer(event.getTo()))) {
            PacketDistributor.sendToPlayer(serverPlayer, new EquipmentChangedPacket());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Utils.serverSideCancelCast(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                Utils.serverSideCancelCast(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void handleUpgradeModifiers(ItemAttributeModifierEvent event) {
        UpgradeData upgradeData = UpgradeData.getUpgradeData(event.getItemStack());
        if (upgradeData != UpgradeData.NONE && upgradeData.getUpgradedSlot().equals(event.getSlotType().getName())) {
            UpgradeUtils.handleAttributeEvent(event.getModifiers(), upgradeData, event::addModifier, event::removeModifier, Optional.empty());
        }
    }

    @SubscribeEvent
    public static void handleCurioUpgradeModifiers(CurioAttributeModifierEvent event) {
        UpgradeData upgradeData = UpgradeData.getUpgradeData(event.getItemStack());
        if (upgradeData != UpgradeData.NONE && upgradeData.getUpgradedSlot().equals(event.getSlotContext().identifier())) {
//        IronsSpellbooks.LOGGER.debug("handleCurioUpgradeModifiers slot: {} uuid: {}",event.getSlotContext().getIdentifier(), event.getUuid());
            UpgradeUtils.handleAttributeEvent(event.getModifiers(), upgradeData, event::addModifier, event::removeModifier, Optional.of(event.getUuid()));
        }
    }

    @SubscribeEvent
    public static void onExperienceDroppedEvent(LivingExperienceDropEvent event) {
        var player = event.getAttackingPlayer();
        if (player != null) {
            int ringCount = CuriosApi.getCuriosInventory(player).map(inventory -> inventory.findCurios(ItemRegistry.EMERALD_STONEPLATE_RING.get()).size()).orElse(0);
            for (int i = 0; i < ringCount; i++) {
                event.setDroppedExperience((int) (event.getDroppedExperience() * 1.25));
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && event.getTarget() instanceof ServerPlayer targetPlayer) {
            MagicData.getPlayerMagicData(serverPlayer).getSyncedData().syncToPlayer(targetPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
            playerMagicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
            playerMagicData.getPlayerRecasts().syncAllToPlayer();
            playerMagicData.getSyncedData().syncToPlayer(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(playerMagicData));
            CameraShakeManager.doSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerStartTrackingEntity(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayerRecipient) {
            if (event.getTarget() instanceof LivingEntity livingEntity) {
                for (var inst : livingEntity.getActiveEffects()) {
                    if (inst.getEffect() instanceof ISyncedMobEffect) {
                        serverPlayerRecipient.connection.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), inst));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!entity.level.isClientSide) {
            if (entity instanceof ServerPlayer serverPlayer) {
                Utils.serverSideCancelCast(serverPlayer);
                MagicData.getPlayerMagicData(serverPlayer).getPlayerRecasts().removeAll(RecastResult.DEATH);
            }
            entity.getActiveEffects().forEach(mobEffectInstance -> {
                if (mobEffectInstance.getEffect() instanceof IMobEffectEndCallback callback) {
                    callback.onEffectRemoved(entity, mobEffectInstance.getAmplifier());
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellTeleport(SpellTeleportEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (ItemRegistry.TELEPORTATION_AMULET.get().isEquippedBy(livingEntity)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.EVASION.get(), 3 * 20, 0, false, false, true));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newServerPlayer) {
            event.getOriginal().getActiveEffects().forEach((effect -> {
                //IronsSpellbooks.LOGGER.debug("{}", effect.getEffect().getDisplayName().getString());
                if (effect.getEffect() instanceof SummonTimer) {
                    newServerPlayer.addEffect(effect, newServerPlayer);
                }
            }));

            MagicData oldMagicData = MagicData.getPlayerMagicData(event.getOriginal());
            MagicData newMagicData = MagicData.getPlayerMagicData(newServerPlayer);
            newMagicData.setSyncedData(oldMagicData.getSyncedData().getPersistentData(newServerPlayer));
            oldMagicData.getPlayerCooldowns().getSpellCooldowns().forEach((spellId, cooldown) -> newMagicData.getPlayerCooldowns().getSpellCooldowns().put(spellId, cooldown));
            //newMagicData.getSyncedData().syncToPlayer(newServerPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Utils.serverSideCancelCast(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {

            //Clear fire and frozen
            serverPlayer.clearFire();
            serverPlayer.setTicksFrozen(0);
            var data = serverPlayer.getEntityData().packDirty();
            if (data != null) {
                serverPlayer.connection.send(new ClientboundSetEntityDataPacket(serverPlayer.getId(), data));
            }

            //Cancel casting
            Utils.serverSideCancelCast(serverPlayer);

            //Set respawn mana
            MagicData.getPlayerMagicData(serverPlayer).setMana((int) (serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA.get()) * ServerConfigs.MANA_SPAWN_PERCENT.get()));
        }
    }

    @SubscribeEvent
    public static void fixDragonCrits(CriticalHitEvent event) {
        if (event.getTarget().level.isClientSide) {
            return;
        }
        // Crits require the target to be a LivingEntity, meaning dragon parts cannot be critically struck
        // Re-evaluate default crit criteria (without the living entity check, ofc)
        if (event.getTarget() instanceof ICritablePartEntity dragonPartEntity) {
            var part = (Entity) dragonPartEntity;
            var attacker = event.getEntity();
            var defaultShouldCrit = attacker.getAttackStrengthScale(0.5f) > .9
                    && attacker.fallDistance > 0.0F
                    && !attacker.onGround()
                    && !attacker.onClimbable()
                    && !attacker.isInWater()
                    && !attacker.hasEffect(MobEffects.BLINDNESS)
                    && !attacker.isPassenger()
                    && !attacker.isSprinting();
            if (defaultShouldCrit) {
                if (event.getDamageModifier() == 1) {
                    event.setDamageModifier(1.5f);
                }
                // crit particles won't play on nonliving entities, do them manually
                var boundingBox = part.getBoundingBox();
                Vec3 vec3 = boundingBox.getCenter();
                MagicManager.spawnParticles(event.getEntity().level, ParticleTypes.CRIT, vec3.x, vec3.y, vec3.z, 25, boundingBox.getXsize() * .6, boundingBox.getYsize() * .6, boundingBox.getZsize() * .6, 0, false);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingAttackEvent event) {
        var livingEntity = event.getEntity();
        //irons_spellbooks.LOGGER.debug("onLivingAttack.1: {}", livingEntity);
        if (event.getSource().getEntity() != null && livingEntity.getVehicle() instanceof IceTombEntity iceTomb && !DamageSources.isFriendlyFireBetween(event.getSource().getEntity(), livingEntity)) {
            // redirect entity-caused damage away from entombed players into the tomb
            event.setCanceled(true);
            iceTomb.hurt(event.getSource(), event.getAmount());
            return;
        }
        if ((livingEntity instanceof ServerPlayer) || (livingEntity instanceof IMagicEntity)) {
            if (ItemRegistry.FIREWARD_RING.get().isEquippedBy(livingEntity) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
                event.getEntity().clearFire();
                event.setCanceled(true);
                return;
            }
            var playerMagicData = MagicData.getPlayerMagicData(livingEntity);
            if (livingEntity.hasEffect(MobEffectRegistry.EVASION.get())) {
                if (EvasionEffect.doEffect(livingEntity, event.getSource())) {
                    event.setCanceled(true);
                    return;
                }
            } else if (livingEntity.hasEffect(MobEffectRegistry.ABYSSAL_SHROUD.get())) {
                if (AbyssalShroudEffect.doEffect(livingEntity, event.getSource())) {
                    event.setCanceled(true);
                    return;
                }
            }

            if (livingEntity instanceof ServerPlayer serverPlayer) {
                if (playerMagicData.isCasting() &&
                        playerMagicData.getCastingSpell().getSpell().canBeInterrupted(serverPlayer) &&
                        playerMagicData.getCastDurationRemaining() > 0 &&
                        !event.getSource().is(DamageTypeTagGenerator.LONG_CAST_IGNORE) &&
                        !playerMagicData.popMarkedPoison()) {
                    Utils.serverSideCancelCast(serverPlayer);
                }
            }
        }
        if (ServerConfigs.BETTER_CREEPER_THUNDERHIT.get() && event.getSource().is(DamageTypeTags.IS_FIRE) && event.getEntity() instanceof Creeper creeper && creeper.isPowered()) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onBeforeDamageTaken(LivingDamageEvent event) {
        var livingEntity = event.getEntity();
        if (livingEntity instanceof IMagicEntity || livingEntity instanceof ServerPlayer) {
            var playerMagicData = MagicData.getPlayerMagicData(livingEntity);
            if (livingEntity.hasEffect(MobEffectRegistry.HEARTSTOP.get())) {
                playerMagicData.getSyncedData().addHeartstopDamage(event.getAmount() * .5f);
                event.setAmount(0);
            }
        }
        if (event.getSource().is(ISSDamageTypes.FIRE_MAGIC) && event.getSource().getEntity() instanceof LivingEntity livingAttacker) {
            if (livingAttacker.getItemBySlot(EquipmentSlot.CHEST).is(ItemRegistry.INFERNAL_SORCERER_CHESTPLATE.get()) && (!(livingAttacker instanceof Player player) || !player.getCooldowns().isOnCooldown(ItemRegistry.INFERNAL_SORCERER_CHESTPLATE.get()))) {
                ImmolateEffect.addImmolateStack(livingEntity, livingAttacker);
//                if (livingAttacker instanceof Player player) {
//                    player.getCooldowns().addCooldown(ItemRegistry.INFERNAL_SORCERER_CHESTPLATE.get(), Utils.applyCooldownReduction(InfernalSorcererArmorItem.COOLDOWN_TICKS, player));
//                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        var newTarget = event.getNewTarget();
        var entity = event.getEntity();
        if (newTarget != null) {
            //Prevent Village allies (ie preists/iron golems) from aggroing eachother
            if (newTarget.getType().is(ModTags.VILLAGE_ALLIES) && entity.getType().is(ModTags.VILLAGE_ALLIES)) {
                event.setCanceled(true);
                return;
            }
            //Prevent mobs who auto-target hostile mobs from targeting "enemy" summons, unless they are actually fighting
            if (newTarget instanceof IMagicSummon summon && summon instanceof Enemy && !(entity.equals(((Mob) newTarget).getTarget()))) {
                event.setCanceled(true);
                return;
            }
            if (newTarget.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get())) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void preventDismount(EntityMountEvent event) {
        var mount = event.getEntityBeingMounted();
        var entity = event.getEntity();
        if (!entity.level.isClientSide && event.isDismounting() && mount instanceof PreventDismount preventDismount
                && !mount.isRemoved() && !entity.isRemoved()) {
            if (!preventDismount.canEntityDismount(entity)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
            var victim = entityHitResult.getEntity();
            if (victim instanceof IMagicEntity || victim instanceof Player) {
                var livingEntity = (LivingEntity) victim;
                if (livingEntity.hasEffect(MobEffectRegistry.EVASION.get())) {
                    if (EvasionEffect.doEffect(livingEntity, victim.damageSources().indirectMagic(event.getProjectile(), event.getProjectile().getOwner()))) {
                        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                    }
                } else if (livingEntity.hasEffect(MobEffectRegistry.ABYSSAL_SHROUD.get())) {
                    if (AbyssalShroudEffect.doEffect(livingEntity, victim.damageSources().indirectMagic(event.getProjectile(), event.getProjectile().getOwner()))) {
                        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void useOnEntityEvent(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof Creeper creeper) {
            var player = event.getEntity();
            var hand = event.getHand();
            var useItem = player.getItemInHand(hand);
            if (useItem.is(Items.GLASS_BOTTLE) && creeper.isPowered()) {
                creeper.hurt(creeper.damageSources().generic(), 5);
                player.level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                player.swing(hand);
                player.setItemInHand(hand, ItemUtils.createFilledResult(useItem, player, new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get())));
                event.setCancellationResult(InteractionResultHolder.consume(player.getItemInHand(hand)).getResult());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void handleResistanceAttributesOnSpawn(MobSpawnEvent.FinalizeSpawn event) {
        var mob = event.getEntity();
        //Attributes should never be null because all living entities have these attributes
        if (mob.getMobType() == MobType.UNDEAD) {
            //Undead take extra holy damage, and less blood (necromantic) damage
            setIfNonNull(mob, AttributeRegistry.HOLY_MAGIC_RESIST, 0.5);
            setIfNonNull(mob, AttributeRegistry.BLOOD_MAGIC_RESIST, 1.5);
        } else if (mob.getMobType() == MobType.WATER) {
            //Water mobs take extra lightning damage
            setIfNonNull(mob, AttributeRegistry.LIGHTNING_MAGIC_RESIST, 0.5);
        }
        if (mob.fireImmune()) {
            //Fire immune (blazes, pyromancer, etc) take 50% fire damage
            setIfNonNull(mob, AttributeRegistry.FIRE_MAGIC_RESIST, 1.5);
        }
        //TODO: replace this with "fire_elemental" entity tag for all fiery mobs (blaze, magma cubes, modded mobs)
        if (mob.getType() == EntityType.BLAZE) {
            setIfNonNull(mob, AttributeRegistry.ICE_MAGIC_RESIST, 0.5);
        }
    }

    private static void setIfNonNull(LivingEntity mob, Supplier<Attribute> attribute, double value) {
        var instance = mob.getAttributes().getInstance(attribute.get());
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        var level = entity.level;
        if (!level.isClientSide) {
            if (entity.tickCount % 40 == 0) {
                BlockPos pos = entity.blockPosition();
                BlockState blockState = entity.level.getBlockState(pos);
                if (blockState.is(Blocks.CAULDRON)) {
                    BloodCauldronBlock.attemptCookEntity(blockState, entity.level, pos, entity, () -> {
                        level.setBlockAndUpdate(pos, BlockRegistry.BLOOD_CAULDRON_BLOCK.get().defaultBlockState());
                        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    });
                }
            }
        }
    }

    // uses mixin for 1.20.1
//    @SubscribeEvent
//    public static void registerPatrolSpawners(ModifyCustomSpawnersEvent event) {
//        if (event.getLevel().dimension().equals(Level.OVERWORLD)) {
//            event.addCustomSpawner(new IceSpiderPatrolSpawner());
//        }
//    }

    @SubscribeEvent
    public static void onAnvilRecipe(AnvilUpdateEvent event) {
        //IronsSpellbooks.LOGGER.debug("onAnvilRecipe");
        if (event.getRight().is(ItemRegistry.SHRIVING_STONE.get())) {
            //IronsSpellbooks.LOGGER.debug("shriving stone");
            var result = Utils.handleShriving(event.getLeft());
            if (!result.isEmpty()) {
//                var itemName = event.getName();
//                if (itemName != null && !StringUtil.isBlank(itemName)) {
//                    if (!itemName.equals(result.getHoverName().getString())) {
//                        result.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
//                    }
//                } else if (result.has(DataComponents.CUSTOM_NAME)) {
//                    result.remove(DataComponents.CUSTOM_NAME);
//                }
                event.setOutput(result);
                event.setCost(1);
                event.setMaterialCost(1);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ServerConfigs.PORTAL_FRAME_RESTRICT_BREAKING.get()) {
            if (event.getState().is(BlockRegistry.PORTAL_FRAME.get())) {
                var player = event.getPlayer();
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof PortalFrameBlockEntity portalFrameBlockEntity && portalFrameBlockEntity.getOwnerUUID() != null && !player.getUUID().equals(portalFrameBlockEntity.getOwnerUUID())) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.portal_break_failure").withStyle(ChatFormatting.RED)));
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void preventBlockPlacement(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        if (level.dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
            if (event.getItemStack().getItem() instanceof BlockItem blockItem && blockItem.getBlock().builtInRegistryHolder().is(ModTags.PREVENT_POCKET_DIMENSION_PLACEMENT)) {
                event.setCanceled(true);
                if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.translatable("ui.irons_spellbooks.error_place_block_dimension").withStyle(ChatFormatting.RED), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void preventPocketDimensionTeleportation(EntityTeleportEvent event) {
        if (event.getEntity().level instanceof ServerLevel serverLevel && serverLevel.dimension().equals(PocketDimensionManager.POCKET_DIMENSION) && !(event instanceof EntityTeleportEvent.TeleportCommand)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void changeDigSpeed(PlayerEvent.BreakSpeed event) {
        //This event is getting run on the server and the client, and because the client is aware of its own status effects, this works
        //(If it did not get run on the client, then breaking particles would not match)
        var player = event.getEntity();
        if (player.hasEffect(MobEffectRegistry.HASTENED.get())) {
            int i = 1 + player.getEffect(MobEffectRegistry.HASTENED.get()).getAmplifier();
            event.setNewSpeed(event.getNewSpeed() * Utils.intPow(1.2f, i));
        }
        if (player.hasEffect(MobEffectRegistry.SLOWED.get())) {
            int i = 1 + player.getEffect(MobEffectRegistry.SLOWED.get()).getAmplifier();
            event.setNewSpeed(event.getNewSpeed() * Utils.intPow(.8f, i));
        }
    }

    @SubscribeEvent
    public static void changeBreedOutcome(BabyEntitySpawnEvent event) {
        if (ServerConfigs.HOGLIN_OFFSPRING_PROTECTION.get()) {
            if (event.getChild() instanceof Hoglin baby && event.getParentA() instanceof Hoglin parent1 && event.getParentB() instanceof Hoglin parent2) {
                double i = (parent1.isImmuneToZombification() ? 0.5 : 0) + (parent2.isImmuneToZombification() ? 0.5 : 0);
                //produces: 0% if neither, 50% if 1, 100% if both
                if (Utils.random.nextFloat() < i) {
                    baby.setImmuneToZombification(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChangeDimensions(EntityTravelToDimensionEvent event) {
        var entity = event.getEntity();
        if (!(entity.level instanceof ServerLevel serverLevel)) {
            return;
        }
        /*
         * Disallow summons to change dimensions
         */
        var owner = SummonManager.getOwner(entity);
        if (owner != null) {
            event.setCanceled(true);
            return;
        }
        /*
         * Destroy all of our summons when we teleport. We don't have enough context to bring them with us, and we cannot leave them, so they must die
         */
        var summons = SummonManager.getSummons(entity);
        if (!summons.isEmpty()) {
            for (UUID uuid : summons) {
                var summon = serverLevel.getEntity(uuid);
                if (summon instanceof IMagicSummon magicSummon) {
                    magicSummon.onUnSummon();
                } else if (summon != null) {
                    SummonManager.removeSummon(summon);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDataLoaded(OnDatapackSyncEvent event) {
        // tags only bound on data loaded, so we must wait until now to (dynamically) resolve cauldron interactions
        var map = CauldronInteraction.WATER;
        for (var item : ItemRegistry.getIronsItems()) {
            if (item.get() instanceof DyeableLeatherItem) {
                map.put(item.get(), CauldronInteraction.DYED_ITEM);
            }
        }
    }
}
