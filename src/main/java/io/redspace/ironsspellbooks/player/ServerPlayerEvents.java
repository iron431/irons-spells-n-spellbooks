package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IOminousEntity;
import io.redspace.ironsspellbooks.api.events.SpellTeleportEvent;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.block.BloodCauldronBlock;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PocketDimensionManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.datagen.DamageTypeTagGenerator;
import io.redspace.ironsspellbooks.effect.AbyssalShroudEffect;
import io.redspace.ironsspellbooks.effect.EvasionEffect;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.ImmolateEffect;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.ice_spider.ICritablePartEntity;
import io.redspace.ironsspellbooks.entity.spells.ice_tomb.IceTombEntity;
import io.redspace.skillcasting.data.cast.PreventDismount;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.spell_containers.ImbuedContainer;
import io.redspace.ironsspellbooks.item.spell_containers.SpellbookContainer;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.UpgradeUtils;
import io.redspace.ironsspellbooks.worldgen.IceSpiderPatrolSpawner;
import io.redspace.skillcasting.api.event.BuildCastContextEvent;
import io.redspace.skillcasting.api.event.GatherSkillSelectionEvent;
import io.redspace.skillcasting.api.event.SkillEvent;
import io.redspace.skillcasting.api.event.SkillSelectionPriority;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.ModifyCustomSpawnersEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber
public class ServerPlayerEvents {

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
    public static void handleUpgradeModifiers(ItemAttributeModifierEvent event) {
        UpgradeData upgradeData = UpgradeData.getUpgradeData(event.getItemStack());
        if (upgradeData != UpgradeData.NONE) {
            var equipmentSlot = UpgradeUtils.SLOTS_BY_NAME.get(upgradeData.getUpgradedSlot());
            if (equipmentSlot == null) {
                return;
            }
            var groupSlot = EquipmentSlotGroup.bySlot(equipmentSlot);
            UpgradeUtils.handleAttributeEvent(event.getModifiers(), upgradeData, (atr, mod) -> event.addModifier(atr, mod, groupSlot), (atr, mod) -> event.removeModifier(atr, mod.id()), upgradeData.getUpgradedSlot());
        }
    }

    @SubscribeEvent
    public static void handleCurioUpgradeModifiers(CurioAttributeModifierEvent event) {
        UpgradeData upgradeData = UpgradeData.getUpgradeData(event.getItemStack());
        if (upgradeData != UpgradeData.NONE && upgradeData.getUpgradedSlot().equals(event.getSlotContext().identifier())) {
            var list = event.getModifiers().entries().stream().map(entry -> new ItemAttributeModifiers.Entry(entry.getKey(), entry.getValue(), EquipmentSlotGroup.ANY)).toList();
            UpgradeUtils.handleAttributeEvent(list, upgradeData, event::addModifier, event::removeModifier, event.getSlotContext().identifier());
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
        if (event.getEntity() instanceof ServerPlayer serverPlayerRecipient) {
            if (event.getTarget() instanceof LivingEntity livingEntity) {
                for (var inst : livingEntity.getActiveEffects()) {
                    if (inst.getEffect().value() instanceof ISyncedMobEffect) {
                        serverPlayerRecipient.connection.send(new ClientboundUpdateMobEffectPacket(livingEntity.getId(), inst, false));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CameraShakeManager.doSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!entity.level.isClientSide) {
            entity.getActiveEffects().forEach(mobEffectInstance -> {
                if (mobEffectInstance.getEffect().value() instanceof IMobEffectEndCallback callback) {
                    callback.onEffectRemoved(entity, mobEffectInstance.getAmplifier());
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpellTeleport(SpellTeleportEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (event.getSpell() != SpellRegistry.EVASION_SPELL.get() && ItemRegistry.TELEPORTATION_AMULET.get().isEquippedBy(livingEntity)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.EVASION, 3 * 20, 0, false, false, true));
            }
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
                // fixme: this forced packet was to fix client desync somehow caused by our spell effects. still necessary?
                serverPlayer.connection.send(new ClientboundSetEntityDataPacket(serverPlayer.getId(), data));
            }

            //Set respawn mana
            // fixme: gear bonuses have not affected max mana yet (keep inventory)
            MagicData.get(serverPlayer).setMana((int) (serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA) * ServerConfigs.MANA_SPAWN_PERCENT.get()));
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
            // duplicate of vanilla logic
            var defaultShouldCrit = attacker.getAttackStrengthScale(0.5f) > .9
                    && attacker.fallDistance > 0.0F
                    && !attacker.onGround()
                    && !attacker.onClimbable()
                    && !attacker.isInWater()
                    && !attacker.hasEffect(MobEffects.BLINDNESS)
                    && !attacker.isPassenger()
                    && !attacker.isSprinting();
            if (defaultShouldCrit) {
                event.setCriticalHit(true);
                if (event.getDamageMultiplier() == 1) {
                    event.setDamageMultiplier(1.5f);
                }
                // crit particles won't play on nonliving entities, do them manually
                var boundingBox = part.getBoundingBox();
                Vec3 vec3 = boundingBox.getCenter();
                MagicManager.spawnParticles(event.getEntity().level, ParticleTypes.CRIT, vec3.x, vec3.y, vec3.z, 25, boundingBox.getXsize() * .6, boundingBox.getYsize() * .6, boundingBox.getZsize() * .6, 0, false);
            }
        }
    }

    @SubscribeEvent
    public static void buildSkillOptions(GatherSkillSelectionEvent event) {
        var player = event.getEntity();
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            ItemStack spellbook = Utils.getPlayerSpellbookStack(player);
            if (spellbook != null && SpellbookContainer.has(spellbook)) {
                event.addSource(SpellbookContainer.get(spellbook), Curios.SPELLBOOK_SLOT, SkillSelectionPriority.PRIMARY_SKILL_SOURCE);
            }
            inv.findCurios(ImbuedContainer::has).stream().filter(slot -> !slot.slotContext().identifier().equals(Curios.SPELLBOOK_SLOT)).forEach(
                    slotResult -> event.addSource(ImbuedContainer.get(slotResult.stack()), String.format("%s_%s", slotResult.slotContext().identifier(), slotResult.slotContext().index()), SkillSelectionPriority.CURIO));
        });
    }

    @SubscribeEvent
    public static void onCurioChangeEvent(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (ImbuedContainer.has(event.getFrom()) || ImbuedContainer.has(event.getTo()) || SpellbookContainer.has(event.getFrom()) || SpellbookContainer.has(event.getTo())) {
            SkillcastingData.get(player).selectionManager().refresh(player);
            SkillcastingNetwork.syncSelection(player, SkillcastingData.get(player));
        }
    }

    @SubscribeEvent
    public static void onCastComplete(SkillEvent.OnCastComplete event) {
        if (event.getCastContext().asEntityCaster() instanceof ServerPlayer serverPlayer &&
                (event.getCastEndReason() == CastEndReason.COMPLETED || event.getCastContext().skill().value().getCastType() == CastType.CONTINUOUS)) {
            Scroll.attemptRemoveScrollAfterCast(serverPlayer, event.getCastContext());
        }
    }

    @SubscribeEvent
    public static void buildSpellLevel(BuildCastContextEvent.Level event) {
        // todo: in the purview of this being only spells, should this just be moved to abstractspell's build context?
        CastContext context = event.context();
        if (context.asEntityCaster() instanceof LivingEntity livingEntity && context.skill().value() instanceof AbstractSpell spell) {
            int affinityBonus = CuriosApi.getCuriosInventory(livingEntity).map(inv ->
                    inv.findCurios(AffinityData::hasAffinityData).stream()
                            .mapToInt(slot -> AffinityData.getAffinityData(slot.stack()).getBonusFor(spell)).sum()).orElse(0);
            event.setLevel(event.getLevel() + affinityBonus);
        }
    }

    @SubscribeEvent
    public static void onPlayerDropItem(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        var itemStack = event.getEntity().getItem();
        if (itemStack.getItem() instanceof Scroll) {
            var castingData = SkillcastingData.get(serverPlayer);
            if (castingData.isCasting() && castingData.getActiveCastType() == CastType.CONTINUOUS) {
                castingData.getActiveCast().context().find(SpellcastingComponentTypes.SCROLL_STACK)
                        .ifPresent(stack -> {
                            Scroll.removeScrollAfterCast(serverPlayer, itemStack);
                            castingData.getActiveCast().context().remove(SpellcastingComponentTypes.SCROLL_STACK);
                        });
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        var livingEntity = event.getEntity();
        if (event.getSource().getEntity() != null && livingEntity.getVehicle() instanceof IceTombEntity iceTomb && !DamageSources.isFriendlyFireBetween(event.getSource().getEntity(), livingEntity)) {
            // redirect entity-caused damage away from entombed players into the tomb
            event.setCanceled(true);
            iceTomb.hurt(event.getSource(), event.getOriginalAmount());
            return;
        }
        if (ItemRegistry.FIREWARD_RING.get().isEquippedBy(livingEntity) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.getEntity().clearFire();
            event.setCanceled(true);
            return;
        }
        var magicData = MagicData.get(livingEntity);
        if (livingEntity.hasEffect(MobEffectRegistry.EVASION)) {
            if (EvasionEffect.doEffect(livingEntity, event.getSource())) {
                event.setCanceled(true);
                return;
            }
        } else if (livingEntity.hasEffect(MobEffectRegistry.ABYSSAL_SHROUD)) {
            if (AbyssalShroudEffect.doEffect(livingEntity, event.getSource())) {
                event.setCanceled(true);
                return;
            }
        }

        if (livingEntity instanceof ServerPlayer player) {
            SkillcastingData data = SkillcastingData.get(player);
            if (data.isCasting() && data.getActiveSkill() instanceof AbstractSpell spell &&
                    spell.canBeInterrupted(player) &&
                    !event.getSource().is(DamageTypeTagGenerator.LONG_CAST_IGNORE)) {
                SkillcastingManager.cancelCast(CasterRef.entity(player), CastEndReason.INTERRUPTED);
            }
        }
        if (ServerConfigs.BETTER_CREEPER_THUNDERHIT.get() && event.getSource().is(DamageTypeTags.IS_FIRE) && event.getEntity() instanceof Creeper creeper && creeper.isPowered()) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onBeforeDamageTaken(LivingDamageEvent.Pre event) {
        var livingEntity = event.getEntity();
        // fixme: unguarded magic data get
        var magicData = MagicData.get(livingEntity);
        if (livingEntity.hasEffect(MobEffectRegistry.HEARTSTOP)) {
            magicData.setHeartStopAccumulatedDamage(magicData.getHeartStopAccumulatedDamage() + event.getOriginalDamage() * .5f);
            event.setNewDamage(0);
        }
        if (event.getSource().is(ISSDamageTypes.FIRE_MAGIC) && event.getSource().getEntity() instanceof LivingEntity livingAttacker) {
            if (livingAttacker.getItemBySlot(EquipmentSlot.CHEST).is(ItemRegistry.INFERNAL_SORCERER_CHESTPLATE) && (!(livingAttacker instanceof Player player) || !player.getCooldowns().isOnCooldown(ItemRegistry.INFERNAL_SORCERER_CHESTPLATE.get()))) {
                ImmolateEffect.addImmolateStack(livingEntity, livingAttacker);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        var newTarget = event.getNewAboutToBeSetTarget();
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
            if (newTarget.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY)) {
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
            if (victim instanceof LivingEntity livingEntity) {
                if (livingEntity.hasEffect(MobEffectRegistry.EVASION)) {
                    if (EvasionEffect.doEffect(livingEntity, victim.damageSources().indirectMagic(event.getProjectile(), event.getProjectile().getOwner()))) {
                        event.setCanceled(true);
                    }
                } else if (livingEntity.hasEffect(MobEffectRegistry.ABYSSAL_SHROUD)) {
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
    public static void handleResistanceAttributesOnSpawn(FinalizeSpawnEvent event) {
        var mob = event.getEntity();
        //Attributes should never be null because all living entities have these attributes
        if (mob.getType().is(EntityTypeTags.UNDEAD)) {
            //Undead take extra holy damage, and less blood (necromantic) damage
            setIfNonNull(mob, AttributeRegistry.HOLY_MAGIC_RESIST, 0.5);
            setIfNonNull(mob, AttributeRegistry.BLOOD_MAGIC_RESIST, 1.5);
        } else if (mob.getType().is(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
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

    private static void setIfNonNull(LivingEntity mob, Holder<Attribute> attribute, double value) {
        var instance = mob.getAttributes().getInstance(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
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
    public static void registerPatrolSpawners(ModifyCustomSpawnersEvent event) {
        if (event.getLevel().dimension().equals(Level.OVERWORLD)) {
            event.addCustomSpawner(new IceSpiderPatrolSpawner());
        }
    }

    @SubscribeEvent
    public static void onAnvilRecipe(AnvilUpdateEvent event) {
        //IronsSpellbooks.LOGGER.debug("onAnvilRecipe");
        if (event.getRight().is(ItemRegistry.SHRIVING_STONE.get())) {
            //IronsSpellbooks.LOGGER.debug("shriving stone");
            var result = Utils.handleShriving(event.getLeft());
            if (!result.isEmpty()) {
                var itemName = event.getName();
                if (itemName != null && !StringUtil.isBlank(itemName)) {
                    if (!itemName.equals(result.getHoverName().getString())) {
                        result.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
                    }
                } else if (result.has(DataComponents.CUSTOM_NAME)) {
                    result.remove(DataComponents.CUSTOM_NAME);
                }
                event.setOutput(result);
                event.setCost(1);
                event.setMaterialCost(1);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ServerConfigs.PORTAL_FRAME_RESTRICT_BREAKING.get()) {
            if (event.getState().is(BlockRegistry.PORTAL_FRAME)) {
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
        if (event.getLevel() instanceof Level level && level.dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
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

    @SubscribeEvent
    public static void handleOminousEntities(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || event.loadedFromDisk()) {
            return;
        }
        var entity = event.getEntity();
        if (entity instanceof IOminousEntity ominousSettings && !ominousSettings.isOminous() && ominousSettings.canTriggerOminous()) {
            float rangeSqr = ominousSettings.ominousTriggerRange();
            rangeSqr *= rangeSqr;
            Vec3 center = entity.position();
            List<Player> ominousPlayers = new ArrayList<>();
            for (Player player : serverLevel.players()) {
                if (player.isCreative() || player.isSpectator() || player.distanceToSqr(center) > rangeSqr) {
                    continue;
                }
                if (player.hasEffect(MobEffects.TRIAL_OMEN)) {
                    ominousPlayers.add(player);
                } else if (player.hasEffect(MobEffects.BAD_OMEN)) {
                    ominousPlayers.add(player);
                    MobEffectInstance mobeffectinstance = player.getEffect(MobEffects.BAD_OMEN);
                    int i = mobeffectinstance.getAmplifier() + 1;
                    int j = 18000 * i;
                    player.removeEffect(MobEffects.BAD_OMEN);
                    player.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, j, 0));
                    MagicManager.spawnParticles(serverLevel, ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY(0.5), player.getZ(), 25, 0.1, 0.2, 0.1, 0.2, false);
                    MagicManager.spawnParticles(serverLevel, ParticleTypes.TRIAL_OMEN, player.getX(), player.getY(0.5), player.getZ(), 25, 0.1, 0.2, 0.1, 0.2, false);
                }
            }
            if (!ominousPlayers.isEmpty()) {
                ominousSettings.onOminousTrigger();
                serverLevel.playSound(null, BlockPos.containing(center), SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundSource.BLOCKS, 4, 1.0F);
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
        var map = CauldronInteraction.WATER.map();
        for (var item : ItemRegistry.getIronsItems()) {
            if (item.is(ItemTags.DYEABLE)) {
                map.put(item.get(), CauldronInteraction.DYED_ITEM);
            }
        }
    }
}
