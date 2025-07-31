package io.redspace.ironsspellbooks.api.spells;

import com.google.common.util.concurrent.AtomicDouble;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.network.ClientboundUpdateCastingState;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnCastFinished;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnCastStarted;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnClientCast;
import io.redspace.ironsspellbooks.player.ClientInputEvents;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.redspace.ironsspellbooks.api.spells.SpellAnimations.*;

public abstract class AbstractSpell {
    private String spellID = null;
    private String deathMessageId = null;
    private String spellName = null;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;

    //All time values in ticks
    protected int castTime;
    //protected int cooldown;

    public AbstractSpell() {
    }

    public final String getSpellName() {
        if (spellName == null) {
            var resourceLocation = Objects.requireNonNull(getSpellResource());
            spellName = resourceLocation.getPath().intern();
        }

        //IronsSpellbooks.LOGGER.debug("AbstractSpell.getSpellName {}", spellName);

        return spellName;
    }

    public final String getSpellId() {
        if (spellID == null) {
            var resourceLocation = Objects.requireNonNull(getSpellResource());
            spellID = resourceLocation.toString().intern();
        }

        return spellID;
    }

    public final ResourceLocation getSpellIconResource() {
        return ResourceLocation.fromNamespaceAndPath(getSpellResource().getNamespace(), "textures/gui/spell_icons/" + getSpellName() + ".png");
    }

    public int getMinRarity() {
        return ServerConfigs.getSpellConfig(this).minRarity().getValue();
    }

    public int getMaxLevel() {
        return ServerConfigs.getSpellConfig(this).maxLevel();
    }

    public int getMinLevel() {
        return 1;
    }

    public MutableComponent getDisplayName(Player player) {
        return Component.translatable(getComponentId());
    }

    public String getComponentId() {
        return String.format("spell.%s.%s", getSpellResource().getNamespace(), getSpellName());
    }

    public abstract ResourceLocation getSpellResource();

    public abstract DefaultConfig getDefaultConfig();

    public abstract CastType getCastType();

    public SchoolType getSchoolType() {
        return ServerConfigs.getSpellConfig(this).school();
    }

    public Vector3f getTargetingColor() {
        return this.getSchoolType().getTargetingColor();
    }

    /**
     * @return Returns the base level plus any casting level bonuses from the caster
     */
    public final int getLevelFor(int level, @Nullable LivingEntity caster) {
        int addition = 0;
        if (caster != null) {
            addition = CuriosApi.getCuriosHelper().findCurios(caster, (itemStack) -> AffinityData.hasAffinityData(itemStack) && AffinityData.getAffinityData(itemStack).getSpell().equals(this)).size();
        }
        var levelEvent = new ModifySpellLevelEvent(this, caster, level, level + addition);
        MinecraftForge.EVENT_BUS.post(levelEvent);
        return levelEvent.getLevel();
    }

    public int getManaCost(int level) {
        return (int) ((baseManaCost + manaCostPerLevel * (level - 1)) * ServerConfigs.getSpellConfig(this).manaMultiplier());
    }

    public int getSpellCooldown() {
        return ServerConfigs.getSpellConfig(this).cooldownInTicks();
    }

    public int getCastTime(int spellLevel) {
        if (this.getCastType() == CastType.INSTANT) {
            return 0;
        }
        return this.castTime;
    }

    public ICastDataSerializable getEmptyCastData() {
        return null;
    }

    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(defaultCastSound());
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastStartAnimation() {
        return switch (getCastType()) {
            case INSTANT -> ANIMATION_INSTANT_CAST;
            case CONTINUOUS -> ANIMATION_CONTINUOUS_CAST;
            case LONG -> ANIMATION_LONG_CAST;
            default -> AnimationHolder.none();
        };
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastFinishAnimation() {
        return switch (getCastType()) {
            case LONG -> ANIMATION_LONG_CAST_FINISH;
            case INSTANT -> AnimationHolder.pass();
            default -> AnimationHolder.none();
        };
    }

    public float getSpellPower(int spellLevel, @Nullable Entity sourceEntity) {

        double entitySpellPowerModifier = 1;
        double entitySchoolPowerModifier = 1;

        float configPowerModifier = (float) ServerConfigs.getSpellConfig(this).powerMultiplier();
        //int level = getLevel(spellLevel, null);
        if (sourceEntity instanceof LivingEntity livingEntity) {
            //level = getLevel(spellLevel, livingEntity);
            entitySpellPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER.get());
            entitySchoolPowerModifier = this.getSchoolType().getPowerFor(livingEntity);
        }

        return (float) ((baseSpellPower + spellPowerPerLevel * (spellLevel - 1)) * entitySpellPowerModifier * entitySchoolPowerModifier * configPowerModifier);
    }

    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 0;
    }

    public float getEntityPowerMultiplier(@Nullable LivingEntity entity) {
        float base = (float) ServerConfigs.getSpellConfig(this).powerMultiplier();
        if (entity == null) {
            return base;
        }
        var entitySpellPowerModifier = (float) entity.getAttributeValue(AttributeRegistry.SPELL_POWER.get());
        var entitySchoolPowerModifier = this.getSchoolType().getPowerFor(entity);
        return (float) (base * entitySpellPowerModifier * entitySchoolPowerModifier);
    }

    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        double entityCastTimeModifier = 1;
        if (entity != null) {
            /*
        Long/Charge casts trigger faster while continuous casts last longer.
        */
            if (getCastType() != CastType.CONTINUOUS) {
                entityCastTimeModifier = 2 - Utils.softCapFormula(entity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION.get()));
            } else {
                entityCastTimeModifier = entity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION.get());
            }
        }

        return Math.round(this.getCastTime(spellLevel) * (float) entityCastTimeModifier);
    }

    /**
     * returns true/false for success/failure to cast
     */
    public boolean attemptInitiateCast(ItemStack stack, int spellLevel, Level level, Player player, CastSource castSource, boolean triggerCooldown, String castingEquipmentSlot) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.attemptInitiateCast isClient:{}, spell{}({})", level.isClientSide, this.getSpellId(), spellLevel);
        }

        if (level.isClientSide) {
            return false;
        }

        var serverPlayer = (ServerPlayer) player;
        var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);

        if (!playerMagicData.isCasting()) {
            CastResult castResult = canBeCastedBy(spellLevel, castSource, playerMagicData, serverPlayer);
            if (castResult.message != null) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(castResult.message));
            }

            if (!castResult.isSuccess() || !checkPreCastConditions(level, spellLevel, serverPlayer, playerMagicData) || MinecraftForge.EVENT_BUS.post(new SpellPreCastEvent(player, this.getSpellId(), spellLevel, getSchoolType(), castSource))) {
                return false;
            }

            if (serverPlayer.isUsingItem()) {
                serverPlayer.stopUsingItem();
            }
            int effectiveCastTime = getEffectiveCastTime(spellLevel, player);

            playerMagicData.initiateCast(this, spellLevel, effectiveCastTime, castSource, castingEquipmentSlot);
            playerMagicData.setPlayerCastingItem(stack);

            onServerPreCast(player.level, spellLevel, player, playerMagicData);
            Messages.sendToPlayer(new ClientboundUpdateCastingState(getSpellId(), spellLevel, effectiveCastTime, castSource, castingEquipmentSlot), serverPlayer);
            Messages.sendToPlayersTrackingEntity(new ClientboundOnCastStarted(serverPlayer.getUUID(), getSpellId(), spellLevel), serverPlayer, true);

            return true;
        } else {
            Utils.serverSideCancelCast(serverPlayer);
            return false;
        }
    }

    public void castSpell(Level world, int spellLevel, ServerPlayer serverPlayer, CastSource castSource, boolean triggerCooldown) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.castSpell isClient:{}, spell{}({})", world.isClientSide, getSpellId(), spellLevel);
        }

        MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
        var playerRecasts = magicData.getPlayerRecasts();
        var playerAlreadyHasRecast = playerRecasts.hasRecastForSpell(getSpellId());

        var event = new SpellOnCastEvent(serverPlayer, this.getSpellId(), spellLevel, getManaCost(spellLevel), this.getSchoolType(), castSource);
        MinecraftForge.EVENT_BUS.post(event);
        if (castSource.consumesMana() && !playerAlreadyHasRecast) {
            var newMana = Math.max(magicData.getMana() - event.getManaCost(), 0);
            magicData.setMana(newMana);
            Messages.sendToPlayer(new ClientboundSyncMana(magicData), serverPlayer);
        }
        onCast(world, event.getSpellLevel(), serverPlayer, castSource, magicData);

        //If onCast just added a recast then don't decrement it

        var playerHasRecastsLeft = playerRecasts.hasRecastForSpell(getSpellId());
        if (playerAlreadyHasRecast && playerHasRecastsLeft) {
            playerRecasts.decrementRecastCount(getSpellId());
        } else if (!playerHasRecastsLeft && triggerCooldown) {
            MagicHelper.MAGIC_MANAGER.addCooldown(serverPlayer, this, castSource);
        }

        Messages.sendToPlayer(new ClientboundOnClientCast(this.getSpellId(), spellLevel, castSource, magicData.getAdditionalCastData()), serverPlayer);
    }

    //Call this at the end of your override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.{}.onRecastFinished, player:{}, recastResult:{}, recastInstance:{}, castDataSerializable:{}",
                    getSpellName(),
                    serverPlayer,
                    recastResult,
                    recastInstance,
                    castDataSerializable);
        }

        MagicHelper.MAGIC_MANAGER.addCooldown(serverPlayer, this, recastInstance.getCastSource());
    }

    /**
     * The primary spell effect sound and particle handling goes here. Called Client Side only
     */
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onClientCast isClient:{}, spell{}({})", level.isClientSide, getSpellId(), spellLevel);
        }
        playSound(getCastFinishSound(), entity);
    }

    /**
     * The primary spell effect handling goes here. Called Server Side
     */
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onCast isClient:{}, spell{}({}), pmd:{}", level.isClientSide, getSpellId(), spellLevel, playerMagicData);
        }

        playSound(getCastFinishSound(), entity);
    }

    /**
     * Checks for if a player is allowed to cast a spell
     */
    public CastResult canBeCastedBy(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player) {
        if (ServerConfigs.DISABLE_ADVENTURE_MODE_CASTING.get()) {
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_adventure").withStyle(ChatFormatting.RED));
            }
        }
        var playerMana = playerMagicData.getMana();

        boolean hasEnoughMana = playerMana - getManaCost(spellLevel) >= 0;
        boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(this);
        boolean hasRecastForSpell = playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId());

        if (castSource == CastSource.SCROLL && this.getRecastCount(spellLevel, player) > 0) {
            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_scroll", getDisplayName(player)).withStyle(ChatFormatting.RED));
        } else if ((castSource == CastSource.SPELLBOOK || castSource == CastSource.SWORD) && isSpellOnCooldown) {
            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_cooldown", getDisplayName(player)).withStyle(ChatFormatting.RED));
        } else if (!hasRecastForSpell && castSource.consumesMana() && !hasEnoughMana) {
            return new CastResult(CastResult.Type.FAILURE, Component.translatable("ui.irons_spellbooks.cast_error_mana", getDisplayName(player)).withStyle(ChatFormatting.RED));
        } else {
            return new CastResult(CastResult.Type.SUCCESS);
        }
    }

    /**
     * Server Side. At this point, the spell is allowed to be cast (mana, cooldown, etc). This checks for limitations of the spell itself, such as if it requires a target but finds none
     */
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return true;
    }

    public void playSound(Optional<SoundEvent> sound, Entity entity) {
        sound.ifPresent((soundEvent -> entity.playSound(soundEvent, 2.0f, .9f + Utils.random.nextFloat() * .2f)));
    }

    private SoundEvent defaultCastSound() {
        return this.getSchoolType().getCastSound();
    }

    /**
     * Called on the server when a spell finishes casting or is cancelled, used for any cleanup or extra functionality
     */
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onServerCastComplete isClient:{}, spell{}({}), pmd:{}, cancelled:{}", level.isClientSide, getSpellId(), spellLevel, playerMagicData, cancelled);
        }

        playerMagicData.resetCastingState();
        if (entity instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayersTrackingEntity(new ClientboundOnCastFinished(serverPlayer.getUUID(), getSpellId(), cancelled), serverPlayer, true);
        }
    }

    /**
     * Called once just before executing onCast. Can be used for client side sounds and particles
     */
    public void onClientPreCast(Level level, int spellLevel, LivingEntity entity, InteractionHand hand, @Nullable MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onClientPreCast isClient:{}, spell{}({}), pmd:{}", level.isClientSide, getSpellId(), spellLevel, playerMagicData);
        }
        if (this.getCastType().immediatelySuppressRightClicks()) {
            if (ClientInputEvents.isUseKeyDown) {
                ClientSpellCastHelper.setSuppressRightClicks(true);
            }
        }
        playSound(getCastStartSound(), entity);
    }

    /**
     * Called once just before executing onCast. Can be used for server side sounds and particles
     */
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onServerPreCast isClient:{}, spell{}({}), pmd:{}", level.isClientSide, getSpellId(), level, playerMagicData);
        }
        playSound(getCastStartSound(), entity);
    }

    /**
     * Called on the server each tick while casting.
     */
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {

    }

    /**
     * Used by AbstractSpellCastingMob to determine if the cast is no longer valid (ie player out of range of a particular spell). Override to create spell-specific criteria
     */
    public boolean shouldAIStopCasting(int spellLevel, Mob mob, LivingEntity target) {
        return false;
    }

    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AbstractSpell other) {
            return this.getSpellResource().equals(other.getSpellResource());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getSpellResource().hashCode();
    }

    private volatile List<Double> rarityWeights;

    private void initializeRarityWeights() {
        synchronized (SpellRegistry.none()) {
            if (rarityWeights == null) {
                int minRarity = getMinRarity();
                int maxRarity = getMaxRarity();
                List<Double> rarityRawConfig = SpellRarity.getRawRarityConfig();
                List<Double> rarityConfig = SpellRarity.getRarityConfig();
                //IronsSpellbooks.LOGGER.debug("rarityRawConfig: {} rarityConfig:{}, {}, {}", rarityRawConfig.size(), rarityConfig.size(), this.hashCode(), this.name());

                List<Double> rarityRawWeights;
                if (minRarity != 0) {
                    //Must balance remaining weights

                    var subList = rarityRawConfig.subList(minRarity, maxRarity + 1);
                    double subtotal = subList.stream().reduce(0d, Double::sum);
                    rarityRawWeights = subList.stream().map(item -> ((item / subtotal) * (1 - subtotal)) + item).toList();

                    var counter = new AtomicDouble();
                    rarityWeights = new ArrayList<>();
                    rarityRawWeights.forEach(item -> {
                        rarityWeights.add(counter.addAndGet(item));
                    });
                } else {
                    //rarityRawWeights = rarityRawConfig;
                    rarityWeights = rarityConfig;
                }
            }
        }
    }

    private final int maxRarity = SpellRarity.LEGENDARY.getValue();

    public SpellRarity getRarity(int level) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int maxLevel = getMaxLevel();
        int maxRarity = getMaxRarity();
        if (maxLevel == 1)
            return SpellRarity.values()[getMinRarity()];
        if (level >= maxLevel) {
            return SpellRarity.LEGENDARY;
        }
        double percentOfMaxLevel = (double) level / (double) maxLevel;

        //irons_spellbooks.LOGGER.debug("getRarity: {} {} {} {} {} {}", this.toString(), rarityRawWeights, rarityWeights, percentOfMaxLevel, minRarity, maxRarity);

        int lookupOffset = maxRarity + 1 - rarityWeights.size();

        for (int i = 0; i < rarityWeights.size(); i++) {
            if (percentOfMaxLevel <= rarityWeights.get(i)) {
                return SpellRarity.values()[i + lookupOffset];
            }
        }

        return SpellRarity.COMMON;
    }

    public String getDeathMessageId() {
        if (deathMessageId == null) {
            deathMessageId = getSpellId().replace(':', '.');
        }

        return deathMessageId;
    }

    public final SpellDamageSource getDamageSource(Entity attacker) {
        return getDamageSource(attacker, attacker);
    }

    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return SpellDamageSource.source(projectile, attacker, this);
    }

    public boolean isEnabled() {
        return ServerConfigs.getSpellConfig(this).enabled();
    }

    public int getMaxRarity() {
        return maxRarity;
    }

    public int getMinLevelForRarity(SpellRarity rarity) {
        if (rarityWeights == null) {
            initializeRarityWeights();
        }

        int minRarity = getMinRarity();
        int maxLevel = getMaxLevel();
        if (rarity.getValue() < minRarity) {
            return 0;
        }

        if (rarity.getValue() == minRarity) {
            return 1;
        }

        return (int) (rarityWeights.get(rarity.getValue() - (1 + minRarity)) * maxLevel) + 1;
    }

    /**
     * Returns whether this spell can be generated from random loot when no other criteria are specified
     */
    public boolean allowLooting() {
        return true;
    }

    /**
     * Returns an additional condition for whether this spell can be crafted by a player. This does NOT omit it from the scroll forge entirely
     */
    public boolean canBeCraftedBy(Player player) {
        return true;
    }

    /**
     * Returns an additional condition for whether this spell can be crafted in the scroll forge, or whether it will be omitted
     */
    public boolean allowCrafting() {
        return ServerConfigs.getSpellConfig(this).allowCrafting();
    }

    public boolean obfuscateStats(@Nullable Player player) {
        return false;
    }

    public boolean isLearned(@Nullable Player player) {
        return true;
    }

    public boolean needsLearning() {
        return false;
    }

    public boolean canBeInterrupted(@Nullable Player player) {
        return this.getCastType() == CastType.LONG && !ItemRegistry.CONCENTRATION_AMULET.get().isEquippedBy(player);
    }

    public boolean stopSoundOnCancel() {
        return false;
    }
}
