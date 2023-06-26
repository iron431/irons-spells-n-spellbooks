package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.item.IScroll;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.api.item.curios.RingData;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.network.ClientboundUpdateCastingState;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnCastFinished;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnCastStarted;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnClientCast;
import io.redspace.ironsspellbooks.player.ClientInputEvents;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.AnimationHolder;
import io.redspace.ironsspellbooks.util.Log;
import io.redspace.ironsspellbooks.util.Utils;
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
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.builder.ILoopType;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class AbstractSpell {
    public static ResourceLocation ANIMATION_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "animation");

    public static final AnimationHolder ANIMATION_INSTANT_CAST = new AnimationHolder("instant_projectile", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_CONTINUOUS_CAST = new AnimationHolder("continuous_thrust", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    public static final AnimationHolder ANIMATION_CHARGED_CAST = new AnimationHolder("charged_throw", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_LONG_CAST = new AnimationHolder("long_cast", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_LONG_CAST_FINISH = new AnimationHolder("long_cast_finish", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_CONTINUOUS_OVERHEAD = new AnimationHolder("continuous_overhead", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    //
    private final SpellType spellType;
    private final CastType castType;
    private int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    //All time values in ticks
    protected int castTime;
    //protected int cooldown;

    public AbstractSpell(SpellType spellType) {
        this.spellType = spellType;
        this.castType = spellType.getCastType();
    }

    public int getID() {
        return this.spellType.getValue();
    }

    public SpellType getSpellType() {
        return this.spellType;
    }

    public SpellRarity getRarity() {
        return spellType.getRarity(getLevel(null));
    }

    public CastType getCastType() {
        return this.castType;
    }

    public SchoolType getSchoolType() {
        return spellType.getSchoolType();
    }

    public int getLevel(@Nullable LivingEntity caster) {
        int addition = 0;
        if (caster != null) {
            addition = CuriosApi.getCuriosHelper().findCurios(caster, this::filterCurios).size();
        }
        return this.level + addition;
    }

    public int getRawLevel() {
        return this.level;
    }

    private boolean filterCurios(ItemStack itemStack) {
        return RingData.hasRingData(itemStack) && RingData.getRingData(itemStack).getSpell() == this.spellType;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getManaCost() {
        return (int) ((baseManaCost + manaCostPerLevel * (getLevel(null) - 1)) * ServerConfigs.getSpellConfig(spellType).manaMultiplier());
    }

    public int getSpellCooldown() {
        return ServerConfigs.getSpellConfig(spellType).cooldownInTicks();
    }

    private int getCastTime() {
        return this.castTime;
    }

    public ICastDataSerializable getEmptyCastData() {
        return null;
    }

    public abstract Optional<SoundEvent> getCastStartSound();

    public abstract Optional<SoundEvent> getCastFinishSound();

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastStartAnimation() {
        return switch (this.castType) {
            case INSTANT -> ANIMATION_INSTANT_CAST;
            case CONTINUOUS -> ANIMATION_CONTINUOUS_CAST;
            case LONG -> ANIMATION_LONG_CAST;
            case CHARGE -> ANIMATION_CHARGED_CAST;
            default -> AnimationHolder.none();
        };
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public AnimationHolder getCastFinishAnimation() {
        return switch (this.castType) {
            case LONG -> ANIMATION_LONG_CAST_FINISH;
            default -> AnimationHolder.none();
        };
    }

    public float getSpellPower(@Nullable Entity sourceEntity) {

        float entitySpellPowerModifier = 1;
        float entitySchoolPowerModifier = 1;

        float configPowerModifier = (float) ServerConfigs.getSpellConfig(spellType).powerMultiplier();
        int level = getLevel(null);
        if (sourceEntity instanceof LivingEntity livingEntity) {
            level = getLevel(livingEntity);
            entitySpellPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER.get());
            switch (this.getSchoolType()) {
                case FIRE -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER.get());
                case ICE -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.ICE_SPELL_POWER.get());
                case LIGHTNING -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get());
                case HOLY -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.HOLY_SPELL_POWER.get());
                case ENDER -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.ENDER_SPELL_POWER.get());
                case BLOOD -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.BLOOD_SPELL_POWER.get());
                case EVOCATION -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.EVOCATION_SPELL_POWER.get());
                case POISON -> entitySchoolPowerModifier = (float) livingEntity.getAttributeValue(AttributeRegistry.POISON_SPELL_POWER.get());
            }
        }


        return (baseSpellPower + spellPowerPerLevel * (level - 1)) * entitySpellPowerModifier * entitySchoolPowerModifier * configPowerModifier;
    }

    public int getEffectiveCastTime(@Nullable LivingEntity entity) {
        double entityCastTimeModifier = 1;
        if (entity != null) {
            /*
        Long/Charge casts trigger faster while continuous casts last longer.
        */
            if (this.castType != CastType.CONTINUOUS)
                entityCastTimeModifier = 2 - Utils.softCapFormula(entity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION.get()));
            else
                entityCastTimeModifier = entity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION.get());
        }

        return Math.round(this.castTime * (float) entityCastTimeModifier);
    }

    public static AbstractSpell getSpell(SpellType spellType, int level) {
        return spellType.getSpellForType(level);
    }

    public static AbstractSpell getSpell(int spellId, int level) {
        return getSpell(SpellType.values()[spellId], level);
    }

    /**
     * returns true/false for success/failure to cast
     */
    public boolean attemptInitiateCast(ItemStack stack, Level level, Player player, CastSource castSource, boolean triggerCooldown) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.attemptInitiateCast isClient:{}, spell{}({})", level.isClientSide, this.spellType, this.getRawLevel());
        }

        if (level.isClientSide) {
            return false;
        }

        var serverPlayer = (ServerPlayer) player;
        var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);

        if (!playerMagicData.isCasting()) {
            int playerMana = playerMagicData.getMana();

            boolean hasEnoughMana = playerMana - getManaCost() >= 0;
            boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(spellType);

            if ((castSource == CastSource.SPELLBOOK || castSource == CastSource.SWORD) && isSpellOnCooldown) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_cooldown", spellType.getDisplayName()).withStyle(ChatFormatting.RED)));
                return false;
            }

            if (castSource.consumesMana() && !hasEnoughMana) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_mana", spellType.getDisplayName()).withStyle(ChatFormatting.RED)));
                return false;
            }

            if (!checkPreCastConditions(level, serverPlayer, playerMagicData))
                return false;

            if (this.castType == CastType.INSTANT) {
                /*
                 * Immediately cast spell
                 */
                castSpell(level, serverPlayer, castSource, triggerCooldown);
            } else if (this.castType == CastType.LONG || this.castType == CastType.CONTINUOUS || this.castType == CastType.CHARGE) {
                /*
                 * Prepare to cast spell (magic manager will pick it up by itself)
                 */
                int effectiveCastTime = getEffectiveCastTime(player);
                playerMagicData.initiateCast(getID(), getLevel(player), effectiveCastTime, castSource);
                onServerPreCast(player.level, player, playerMagicData);
                Messages.sendToPlayer(new ClientboundUpdateCastingState(getID(), getLevel(player), effectiveCastTime, castSource), serverPlayer);
            }

            Messages.sendToPlayersTrackingEntity(new ClientboundOnCastStarted(serverPlayer.getUUID(), spellType), serverPlayer, true);

            return true;
        } else {
            Utils.serverSideCancelCast(serverPlayer);
            return false;
        }
    }

    public void castSpell(Level world, ServerPlayer serverPlayer, CastSource castSource, boolean triggerCooldown) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.castSpell isClient:{}, spell{}({})", world.isClientSide, this.spellType, this.getRawLevel());
        }

        var magicManager = MagicHelper.MAGIC_MANAGER;
        MagicData playerMagicData = MagicData.getPlayerMagicData(serverPlayer);

        if (castSource.consumesMana()) {
            int newMana = playerMagicData.getMana() - getManaCost();
            magicManager.setPlayerCurrentMana(serverPlayer, newMana);
            Messages.sendToPlayer(new ClientboundSyncMana(playerMagicData), serverPlayer);
        }

        if (triggerCooldown) {
            magicManager.addCooldown(serverPlayer, spellType, castSource);
        }

        onCast(world, serverPlayer, playerMagicData);
        Messages.sendToPlayer(new ClientboundOnClientCast(this.getID(), this.getLevel(serverPlayer), castSource, playerMagicData.getAdditionalCastData()), serverPlayer);

        if (this.castType == CastType.INSTANT) {
            onServerCastComplete(world, serverPlayer, playerMagicData, false);
        }

        if (serverPlayer.getMainHandItem().getItem() instanceof ISpellbook || serverPlayer.getMainHandItem().getItem() instanceof IScroll)
            playerMagicData.setPlayerCastingItem(serverPlayer.getMainHandItem());
        else
            playerMagicData.setPlayerCastingItem(serverPlayer.getOffhandItem());

    }

    /**
     * The primary spell effect sound and particle handling goes here. Called Client Side only
     */
    public void onClientCast(Level level, LivingEntity entity, ICastData castData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onClientCast isClient:{}, spell{}({})", level.isClientSide, this.spellType, this.getRawLevel());
        }

        playSound(getCastFinishSound(), entity, true);
        if (ClientInputEvents.isUseKeyDown) {
            if (this.spellType.getCastType().holdToCast()) {
                ClientSpellCastHelper.setSuppressRightClicks(true);
            }
            ClientInputEvents.hasReleasedSinceCasting = false;
        }
    }

    /**
     * The primary spell effect handling goes here. Called Server Side
     */
    public void onCast(Level level, LivingEntity entity, MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onCast isClient:{}, spell{}({}), pmd:{}", level.isClientSide, this.spellType, this.getRawLevel(), playerMagicData);
        }

        playSound(getCastFinishSound(), entity, true);
    }

    protected void playSound(Optional<SoundEvent> sound, Entity entity, boolean playDefaultSound) {
        if (sound.isPresent()) {
            entity.playSound(sound.get(), 2.0f, .9f + entity.level.random.nextFloat() * .2f);
        } else if (playDefaultSound) {
            entity.playSound(defaultCastSound(), 2.0f, .9f + entity.level.random.nextFloat() * .2f);
        }
    }

    /**
     * Server Side. Used to see if a spell is allowed to be cast, such as if it requires a target but finds none
     */
    public boolean checkPreCastConditions(Level level, LivingEntity entity, MagicData playerMagicData) {
        return true;
    }

    protected void playSound(Optional<SoundEvent> sound, Entity entity) {
        playSound(sound, entity, false);
    }

    private SoundEvent defaultCastSound() {
        return switch (this.getSchoolType()) {

            case FIRE -> SoundRegistry.FIRE_CAST.get();
            case ICE -> SoundRegistry.ICE_CAST.get();
            case LIGHTNING -> SoundRegistry.LIGHTNING_CAST.get();
            case HOLY -> SoundRegistry.HOLY_CAST.get();
            case ENDER -> SoundRegistry.ENDER_CAST.get();
            case BLOOD -> SoundRegistry.BLOOD_CAST.get();
            case EVOCATION -> SoundRegistry.EVOCATION_CAST.get();
            case POISON -> SoundRegistry.POISON_CAST.get();
            default -> SoundRegistry.EVOCATION_CAST.get();
        };
    }

    /**
     * Called on the server when a spell finishes casting or is cancelled, used for any cleanup or extra functionality
     */
    public void onServerCastComplete(Level level, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onServerCastComplete isClient:{}, spell{}({}), pmd:{}, cancelled:{}", level.isClientSide, this.spellType, this.getRawLevel(), playerMagicData, cancelled);
        }

        playerMagicData.resetCastingState();
        if (entity instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayersTrackingEntity(new ClientboundOnCastFinished(serverPlayer.getUUID(), spellType, cancelled), serverPlayer, true);
        }
    }

    /**
     * Called once just before executing onCast. Can be used for client side sounds and particles
     */
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onClientPreCast isClient:{}, spell{}({}), pmd:{}", level.isClientSide, this.spellType, this.getRawLevel(), playerMagicData);
        }
        playSound(getCastStartSound(), entity);
    }

    /**
     * Called once just before executing onCast. Can be used for server side sounds and particles
     */
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onServerPreCast isClient:{}, spell{}({}), pmd:{}", level.isClientSide, this.spellType, this.getRawLevel(), playerMagicData);
        }
        playSound(getCastStartSound(), entity);
    }

    /**
     * Called on the server each tick while casting.
     */
    public void onServerCastTick(Level level, LivingEntity entity, @Nullable MagicData playerMagicData) {

    }

    /**
     * Used by AbstractSpellCastingMob to determine if the cast is no longer valid (ie player out of range of a particular spell). Override to create spell-specific criteria
     */
    public boolean shouldAIStopCasting(Mob mob, LivingEntity target) {
        return false;
    }

    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (o == null)
            return false;
        return this.spellType == o.spellType && this.level == o.level;
    }

}
