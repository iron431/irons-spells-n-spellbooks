package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.block.BloodCauldronBlock;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.capabilities.magic.UpgradeData;
import io.redspace.ironsspellbooks.compat.tetra.TetraProxy;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.data.DataFixerStorage;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.datafix.IronsWorldUpgrader;
import io.redspace.ironsspellbooks.datagen.DamageTypeTagGenerator;
import io.redspace.ironsspellbooks.effect.AbyssalShroudEffect;
import io.redspace.ironsspellbooks.effect.EvasionEffect;
import io.redspace.ironsspellbooks.effect.SpiderAspectEffect;
import io.redspace.ironsspellbooks.effect.SummonTimer;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.SupportMob;
import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.curios.LurkerRing;
import io.redspace.ironsspellbooks.network.ClientboundEquipmentChanged;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.network.ServerboundCancelCast;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Objects;
import java.util.Optional;

@Mod.EventBusSubscriber
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
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        DataFixerStorage.init(event.getServer().storageSource);

        if (/*ServerConfigs.RUN_WORLD_UPGRADER.get()*/false) {
            var server = event.getServer();
            new IronsWorldUpgrader(server.storageSource, server.registries()).runUpgrade();
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);

            if (playerMagicData.isCasting() && (event.getFrom().getItem() instanceof CastingItem || event.getTo().getItem() instanceof CastingItem)) {
                Utils.serverSideCancelCast(serverPlayer);
                Messages.sendToPlayer(new ClientboundEquipmentChanged(), serverPlayer);
                return;
            }

            var isFromSpellContainer = ISpellContainer.isSpellContainer(event.getFrom());
            if (isFromSpellContainer && ISpellContainer.get(event.getFrom()).getIndexForSpell(playerMagicData.getCastingSpell().getSpell()) >= 0) {
                IronsSpellbooks.LOGGER.debug("onLivingEquipmentChangeEvent from:\n{}\n{}", event.getFrom().toString(), Integer.toHexString(event.getFrom().hashCode()));
                IronsSpellbooks.LOGGER.debug("onLivingEquipmentChangeEvent to:\n{}\n{}", event.getTo().toString(), Integer.toHexString(event.getTo().hashCode()));
                if (playerMagicData.isCasting()) {
                    Utils.serverSideCancelCast(serverPlayer);
                }
                Messages.sendToPlayer(new ClientboundEquipmentChanged(), serverPlayer);
            } else if (isFromSpellContainer || ISpellContainer.isSpellContainer(event.getTo())) {
                Messages.sendToPlayer(new ClientboundEquipmentChanged(), serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onCurioChangeEvent(CurioChangeEvent event) {
        var entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer && (ISpellContainer.isSpellContainer(event.getFrom()) || ISpellContainer.isSpellContainer(event.getTo()))) {
            Messages.sendToPlayer(new ClientboundEquipmentChanged(), serverPlayer);
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
        //Ironsspellbooks.logger.debug("onPlayerOpenContainer {} {}", event.getEntity().getName().getString(), event.getContainer().getType());

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
            var ringCount = CuriosApi.getCuriosHelper().findCurios(player, ItemRegistry.EMERALD_STONEPLATE_RING.get()).size();
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
            Messages.sendToPlayer(new ClientboundSyncMana(playerMagicData), serverPlayer);
            CameraShakeManager.doSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Utils.serverSideCancelCast(serverPlayer);
            MagicData.getPlayerMagicData(serverPlayer).getPlayerRecasts().removeAll(RecastResult.DEATH);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newServerPlayer) {
            boolean keepEverything = !event.isWasDeath();
            //Persist summon timers across death
            event.getOriginal().getActiveEffects().forEach((effect -> {
                //IronsSpellbooks.LOGGER.debug("{}", effect.getEffect().getDisplayName().getString());
                if (effect.getEffect() instanceof SummonTimer) {
                    newServerPlayer.addEffect(effect, newServerPlayer);
                }
            }));
            event.getOriginal().reviveCaps();
            MagicData oldMagicData = MagicData.getPlayerMagicData(event.getOriginal());
            MagicData newMagicData = MagicData.getPlayerMagicData(event.getEntity());
            //TODO: Vanilla does not persist mobeffects, even with keepinventory. Should we?
            newMagicData.setSyncedData(/*keepEverything ? oldMagicData.getSyncedData() : */oldMagicData.getSyncedData().getPersistentData());
            newMagicData.getSyncedData().doSync();
            oldMagicData.getPlayerCooldowns().getSpellCooldowns().forEach((spellId, cooldown) -> newMagicData.getPlayerCooldowns().getSpellCooldowns().put(spellId, cooldown));
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        //Ironsspellbooks.logger.debug("PlayerChangedDimension: {}", event.getEntity().getName().getString());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            //Ironsspellbooks.logger.debug("onPlayerLoggedIn syncing cooldowns to {}", serverPlayer.getName().getString());
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

            //Sync effects
            serverPlayer.getActiveEffects().forEach((effect -> {
                if (effect.getEffect() instanceof SummonTimer) {
                    serverPlayer.connection.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), effect));
                }
            }));

            //Set respawn mana
            MagicData.getPlayerMagicData(serverPlayer).setMana((int) (serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA.get()) * ServerConfigs.MANA_SPAWN_PERCENT.get()));
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        var livingEntity = event.getEntity();
        //irons_spellbooks.LOGGER.debug("onLivingAttack.1: {}", livingEntity);

        if ((livingEntity instanceof ServerPlayer) || (livingEntity instanceof IMagicEntity)) {
            if (ItemRegistry.FIREWARD_RING.get().isEquippedBy(livingEntity) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
                event.getEntity().clearFire();
                event.setCanceled(true);
                return;
            }
            var playerMagicData = MagicData.getPlayerMagicData(livingEntity);
            if (playerMagicData.getSyncedData().hasEffect(SyncedSpellData.EVASION)) {
                if (EvasionEffect.doEffect(livingEntity, event.getSource())) {
                    event.setCanceled(true);
                }
            } else if (playerMagicData.getSyncedData().hasEffect(SyncedSpellData.ABYSSAL_SHROUD)) {
                if (AbyssalShroudEffect.doEffect(livingEntity, event.getSource())) {
                    event.setCanceled(true);
                }
            }
        }
        //TODO: tetra update
        TetraProxy.PROXY.handleLivingAttackEvent(event);
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        var newTarget = event.getNewTarget();
        var entity = event.getEntity();
        if (newTarget != null) {
            //Prevent Village allies (ie preists/iron golems) from aggroing eachother
            if (newTarget.getType().is(ModTags.VILLAGE_ALLIES) && entity.getType().is(ModTags.VILLAGE_ALLIES)) {
                event.setCanceled(true);
            }
            //Prevent mobs who auto-target hostile mobs from targeting "enemy" summons, unless they are actually fighting
            if (newTarget instanceof MagicSummon summon && !(entity.equals(((Mob) newTarget).getTarget()))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTakeDamage(LivingDamageEvent event) {
        if (ServerConfigs.BETTER_CREEPER_THUNDERHIT.get() && event.getSource().is(DamageTypeTags.IS_FIRE) && event.getEntity() instanceof Creeper creeper && creeper.isPowered()) {
            event.setCanceled(true);
            return;
        }
        /*
        Damage Increasing Effects
         */
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            //IronsSpellbooks.LOGGER.debug("onLivingTakeDamage: attacker: {} target:{}", livingAttacker.getName().getString(), event.getEntity());
            //TODO: subscribe in effect class?
            /**
             * Spider aspect handling
             */
            if (livingAttacker.hasEffect(MobEffectRegistry.SPIDER_ASPECT.get())) {
                if (event.getEntity().hasEffect(MobEffects.POISON)) {
                    int lvl = livingAttacker.getEffect(MobEffectRegistry.SPIDER_ASPECT.get()).getAmplifier() + 1;
                    float before = event.getAmount();
                    float multiplier = 1 + SpiderAspectEffect.DAMAGE_PER_LEVEL * lvl;
                    event.setAmount(event.getAmount() * multiplier);
                    //IronsSpellbooks.LOGGER.debug("spider mode {}->{}", before, event.getAmount());

                }
            }
            /**
             * Lurker Ring handling
             */
            if (livingAttacker.isInvisible() && ItemRegistry.LURKER_RING.get().isEquippedBy(livingAttacker)) {
                if (livingAttacker instanceof Player player && !player.getCooldowns().isOnCooldown(ItemRegistry.LURKER_RING.get())) {
                    event.setAmount(event.getAmount() * LurkerRing.MULTIPLIER);
                    player.getCooldowns().addCooldown(ItemRegistry.LURKER_RING.get(), LurkerRing.COOLDOWN_IN_TICKS);
                }
            }
        }
        /*
        Damage Reducing Effects
         */
        var playerMagicData = MagicData.getPlayerMagicData(event.getEntity());
        if (playerMagicData.getSyncedData().hasEffect(SyncedSpellData.HEARTSTOP)) {
            playerMagicData.getSyncedData().addHeartstopDamage(event.getAmount() * .5f);
            //Ironsspellbooks.logger.debug("Accumulated damage: {}", playerMagicData.getSyncedData().getHeartstopAccumulatedDamage());
            event.setCanceled(true);
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (playerMagicData.isCasting() &&
                    playerMagicData.getCastingSpell().getSpell().canBeInterrupted(serverPlayer) &&
                    playerMagicData.getCastDurationRemaining() > 0 &&
                    !event.getSource().is(DamageTypeTagGenerator.LONG_CAST_IGNORE) &&
                    !playerMagicData.popMarkedPoison()) {
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
            var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                Utils.serverSideCancelCast(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void preventDismount(EntityMountEvent event) {
        if (!event.getEntity().level.isClientSide && event.getEntityBeingMounted() instanceof PreventDismount && event.isDismounting() && !event.getEntityBeingMounted().isRemoved()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
            var victim = entityHitResult.getEntity();
            //IronsSpellbooks.LOGGER.debug("onProjectileImpact: {}", victim);
            if (victim instanceof IMagicEntity || victim instanceof Player) {
                //IronsSpellbooks.LOGGER.debug("onProjectileImpact: is a casting mob");
                var livingEntity = (LivingEntity) victim;
                SyncedSpellData syncedSpellData = livingEntity.level.isClientSide ? ClientMagicData.getSyncedSpellData(livingEntity) : MagicData.getPlayerMagicData(livingEntity).getSyncedData();
                if (syncedSpellData.hasEffect(SyncedSpellData.EVASION)) {
                    //IronsSpellbooks.LOGGER.debug("onProjectileImpact: evasion");
                    if (EvasionEffect.doEffect(livingEntity, victim.damageSources().indirectMagic(event.getProjectile(), event.getProjectile().getOwner()))) {
                        event.setCanceled(true);
                    }
                } else if (syncedSpellData.hasEffect(SyncedSpellData.ABYSSAL_SHROUD)) {
                    //IronsSpellbooks.LOGGER.debug("onProjectileImpact: abyssal shroud");
                    if (AbyssalShroudEffect.doEffect(livingEntity, victim.damageSources().indirectMagic(event.getProjectile(), event.getProjectile().getOwner()))) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void useOnEntityEvent(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof Creeper creeper) {
            var player = event.getEntity();
            var useItem = player.getItemInHand(event.getHand());
            if (useItem.is(Items.GLASS_BOTTLE) && creeper.isPowered()) {
                creeper.hurt(creeper.damageSources().generic(), 5);
                player.level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                var hand = event.getHand();
                player.swing(hand);
                player.setItemInHand(hand, ItemUtils.createFilledResult(useItem, player, new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get())));
                event.setCancellationResult(InteractionResultHolder.consume(player.getItemInHand(hand)).getResult());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void useItemEvent(PlayerInteractEvent.RightClickItem event) {
        var entity = event.getEntity();
        if (entity.level.isClientSide) {
            MinecraftInstanceHelper.ifPlayerPresent(localPlayer -> {
                if (ClientMagicData.isCasting() && entity.getUUID().equals(localPlayer.getUUID())) {
                    event.setCanceled(true);
                }
            });
        } else {
            var magicData = MagicData.getPlayerMagicData(entity);
            if (magicData.isCasting() && event.getItemStack() != magicData.getPlayerCastingItem()) {
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
            setIfNonNull(mob, AttributeRegistry.HOLY_MAGIC_RESIST.get(), 0.5);
            setIfNonNull(mob, AttributeRegistry.BLOOD_MAGIC_RESIST.get(), 1.5);
        } else if (mob.getMobType() == MobType.WATER) {
            //Water mobs take extra lightning damage
            setIfNonNull(mob, AttributeRegistry.LIGHTNING_MAGIC_RESIST.get(), 0.5);
        }
        if (mob.fireImmune()) {
            //Fire immune (blazes, pyromancer, etc) take 50% fire damage
            setIfNonNull(mob, AttributeRegistry.FIRE_MAGIC_RESIST.get(), 1.5);
        }
        //TODO: replace this with "fire_elemental" entity tag for all fiery mobs (blaze, magma cubes, modded mobs)
        if (mob.getType() == EntityType.BLAZE) {
            setIfNonNull(mob, AttributeRegistry.ICE_MAGIC_RESIST.get(), 0.5);
        }
    }

    private static void setIfNonNull(LivingEntity mob, Attribute attribute, double value) {
        var instance = mob.getAttributes().getInstance(attribute);
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

    @SubscribeEvent
    public static void onAnvilRecipe(AnvilUpdateEvent event) {
        //IronsSpellbooks.LOGGER.debug("onAnvilRecipe");
        if (event.getRight().is(ItemRegistry.SHRIVING_STONE.get())) {
            //IronsSpellbooks.LOGGER.debug("shriving stone");
            ItemStack newResult = Utils.handleShriving(event.getLeft());
            if (!newResult.isEmpty()) {
                var itemName = event.getName();
                if (itemName != null && !Util.isBlank(itemName)) {
                    if (!itemName.equals(newResult.getHoverName().getString())) {
                        newResult.setHoverName(Component.literal(itemName));
                    }
                } else if (newResult.hasCustomHoverName()) {
                    newResult.resetHoverName();
                }
                event.setOutput(newResult);
                event.setCost(1);
                event.setMaterialCost(1);
                //IronsSpellbooks.LOGGER.debug("new result: {}", newResult);
            }
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
}
