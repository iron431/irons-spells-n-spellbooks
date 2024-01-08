package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.block.BloodCauldronBlock;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.capabilities.magic.UpgradeData;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.compat.tetra.TetraProxy;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.data.DataFixerStorage;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.datafix.IronsWorldUpgrader;
import io.redspace.ironsspellbooks.effect.AbyssalShroudEffect;
import io.redspace.ironsspellbooks.effect.EvasionEffect;
import io.redspace.ironsspellbooks.effect.SpiderAspectEffect;
import io.redspace.ironsspellbooks.effect.SummonTimer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.curios.LurkerRing;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

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
    public static void onServerStartedEvent(ServerStartedEvent event) {
        IronsDataStorage.init(event.getServer().overworld().getDataStorage());
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        DataFixerStorage.init(event.getServer().storageSource);

        if (ServerConfigs.RUN_WORLD_UPGRADER.get()) {
            var server = event.getServer();
            var storageSource = server.storageSource;
            var iwu = new IronsWorldUpgrader(storageSource, server.getWorldData().worldGenSettings());
            iwu.runUpgrade();
            IronsSpellbooks.LOGGER.debug("IWU:{}", iwu.tempCount);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {

        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()
                    && (event.getSlot().getIndex() == 0 || event.getSlot().getIndex() == 1)
                    && (event.getFrom().getItem() instanceof SpellBook || SpellData.hasSpellData(event.getFrom()))) {
                Utils.serverSideCancelCast(serverPlayer);
            }
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
        var itemStack = event.getItemStack();
        if (!UpgradeData.hasUpgradeData(itemStack))
            return;
        var upgradeData = UpgradeData.getUpgradeData(itemStack);

        var slot = event.getSlotType();
        if (upgradeData.getUpgradedSlot() != slot)
            return;

        UpgradeUtils.handleAttributeEvent(event, upgradeData);
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
            playerMagicData.getSyncedData().syncToPlayer(serverPlayer);
            CameraShakeManager.doSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Utils.serverSideCancelCast(serverPlayer);
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
            serverPlayer.connection.send(new ClientboundSetEntityDataPacket(serverPlayer.getId(), serverPlayer.getEntityData(), true));

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

        if ((livingEntity instanceof ServerPlayer) || (livingEntity instanceof AbstractSpellCastingMob)) {
            if (ItemRegistry.FIREWARD_RING.get().isEquippedBy(livingEntity) && event.getSource().isFire()) {
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

        TetraProxy.PROXY.handleLivingAttackEvent(event);
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        var newTarget = event.getNewTarget();
        if (newTarget != null && newTarget.getType().is(ModTags.VILLAGE_ALLIES) && event.getEntity().getType().is(ModTags.VILLAGE_ALLIES)
        ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingTakeDamage(LivingDamageEvent event) {
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
                    event.getSource() != DamageSource.FREEZE &&
                    event.getSource() != DamageSource.STARVE &&
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
            if (victim instanceof AbstractSpellCastingMob || victim instanceof Player) {
                //IronsSpellbooks.LOGGER.debug("onProjectileImpact: is a casting mob");
                var livingEntity = (LivingEntity) victim;
                SyncedSpellData syncedSpellData = livingEntity.level.isClientSide ? ClientMagicData.getSyncedSpellData(livingEntity) : MagicData.getPlayerMagicData(livingEntity).getSyncedData();
                if (syncedSpellData.hasEffect(SyncedSpellData.EVASION)) {
                    //IronsSpellbooks.LOGGER.debug("onProjectileImpact: evasion");
                    if (EvasionEffect.doEffect(livingEntity, new IndirectEntityDamageSource("noop", event.getProjectile(), event.getProjectile().getOwner()))) {
                        event.setCanceled(true);
                    }
                } else if (syncedSpellData.hasEffect(SyncedSpellData.ABYSSAL_SHROUD)) {
                    //IronsSpellbooks.LOGGER.debug("onProjectileImpact: abyssal shroud");
                    if (AbyssalShroudEffect.doEffect(livingEntity, new IndirectEntityDamageSource("noop", event.getProjectile(), event.getProjectile().getOwner()))) {
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
                creeper.hurt(DamageSource.GENERIC.bypassMagic(), 5);
                player.level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                player.swing(event.getHand());
                event.setCancellationResult(InteractionResultHolder.consume(ItemUtils.createFilledResult(useItem, player, new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get()))).getResult());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void handleResistanceAttributesOnSpawn(LivingSpawnEvent.SpecialSpawn event) {
        var mob = event.getEntity();
        //Attributes should never be null because all living entities have these attributes
        if (mob.getMobType() == MobType.UNDEAD) {
            //Undead take extra holy damage, and less blood (necromantic) damage
            mob.getAttributes().getInstance(AttributeRegistry.HOLY_MAGIC_RESIST.get()).setBaseValue(0.5);
            mob.getAttributes().getInstance(AttributeRegistry.BLOOD_MAGIC_RESIST.get()).setBaseValue(1.5);
        } else if (mob.getMobType() == MobType.WATER) {
            //Water mobs take extra lightning damage
            mob.getAttributes().getInstance(AttributeRegistry.LIGHTNING_MAGIC_RESIST.get()).setBaseValue(0.5);
        }
        if (mob.fireImmune()) {
            //Fire immune (blazes, pyromancer, etc) take 50% fire damage
            mob.getAttributes().getInstance(AttributeRegistry.FIRE_MAGIC_RESIST.get()).setBaseValue(1.5);
        }
        if (mob.getType() == EntityType.BLAZE) {
            mob.getAttributes().getInstance(AttributeRegistry.ICE_MAGIC_RESIST.get()).setBaseValue(0.5);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        var level = entity.level;
        if (!level.isClientSide) {
            if (entity.tickCount % 20 == 0) {
                BlockPos pos = entity.blockPosition();
                BlockState blockState = entity.getLevel().getBlockState(pos);
                if (blockState.is(Blocks.CAULDRON)) {
                    BloodCauldronBlock.attemptCookEntity(blockState, entity.getLevel(), pos, entity, () -> {
                        level.setBlockAndUpdate(pos, BlockRegistry.BLOOD_CAULDRON_BLOCK.get().defaultBlockState());
                        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    });
                }
                //TODO: Citadel reimplementation
//                //boolean inStructure = MAGIC_AURA_PREDICATE.matches(serverPlayer.getLevel(), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
//                //boolean inStructure = serverPlayer.getLevel().structureManager().get
//                //var structure = serverPlayer.getLevel().structureManager().getStructureAt(serverPlayer.blockPosition());
//                var structureManager = serverPlayer.getLevel().structureManager();
//                Structure structureKey = structureManager.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).get(ModTags.MAGIC_AURA_TEMP);
//                var structure = structureManager.getStructureAt(serverPlayer.blockPosition(), structureKey);
//                boolean inStructure = structure != StructureStart.INVALID_START /*&& structure.getBoundingBox().isInside(serverPlayer.blockPosition())*/;
//                //IronsSpellbooks.LOGGER.debug("ServerPlayerEvents: In citadel: {}", inStructure);
//                if (inStructure && !ItemRegistry.ENCHANTED_WARD_AMULET.get().isEquippedBy(serverPlayer))
//                    serverPlayer.addEffect(new MobEffectInstance(MobEffectRegistry.ENCHANTED_WARD.get(), 40, 0, false, false, false));

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
