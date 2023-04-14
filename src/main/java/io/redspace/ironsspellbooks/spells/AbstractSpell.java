package io.redspace.ironsspellbooks.spells;

import com.mojang.datafixers.util.Either;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import io.redspace.ironsspellbooks.capabilities.magic.CastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.network.ClientboundCastError;
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
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class AbstractSpell {
    public static ResourceLocation ANIMATION_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "animation");

    private static ResourceLocation ANIMATION_INSTANT_CAST_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "instant_projectile");
    private static ResourceLocation ANIMATION_CONTINUOUS_CAST_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "continuous_thrust");
    private static ResourceLocation ANIMATION_CHARGED_CAST_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "charged_throw");
    private static ResourceLocation ANIMATION_LONG_CAST_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "long_cast");
    private static ResourceLocation ANIMATION_LONG_CAST_FINISH_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "long_cast_finish");

    private final AnimationBuilder ANIMATION_INSTANT_CAST = new AnimationBuilder().addAnimation(ANIMATION_INSTANT_CAST_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder ANIMATION_CONTINUOUS_CAST = new AnimationBuilder().addAnimation(ANIMATION_CONTINUOUS_CAST_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    private final AnimationBuilder ANIMATION_CHARGED_CAST = new AnimationBuilder().addAnimation(ANIMATION_CHARGED_CAST_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder ANIMATION_LONG_CAST = new AnimationBuilder().addAnimation(ANIMATION_LONG_CAST_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder ANIMATION_LONG_CAST_FINISH = new AnimationBuilder().addAnimation(ANIMATION_LONG_CAST_FINISH_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    private final SpellType spellType;
    private final CastType castType;
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    //All time values in ticks
    protected int castTime;
    //protected int cooldown;

    private final LazyOptional<Double> manaMultiplier;
    private final LazyOptional<Double> powerMultiplier;
    private final LazyOptional<Integer> cooldown;

    public AbstractSpell(SpellType spellType) {
        this.spellType = spellType;
        this.castType = spellType.getCastType();

        manaMultiplier = LazyOptional.of(() -> (ServerConfigs.getSpellConfig(spellType).MANA_MULTIPLIER));
        powerMultiplier = LazyOptional.of(() -> (ServerConfigs.getSpellConfig(spellType).POWER_MULTIPLIER));
        cooldown = LazyOptional.of(() -> ((int) (ServerConfigs.getSpellConfig(spellType).COOLDOWN_IN_SECONDS * 20)));
    }

    public int getID() {
        return this.spellType.getValue();
    }

    public SpellType getSpellType() {
        return this.spellType;
    }

    public SpellRarity getRarity() {
        return spellType.getRarity(level);
    }

    public CastType getCastType() {
        return this.castType;
    }

    public SchoolType getSchoolType() {
        return spellType.getSchoolType();
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getManaCost() {
        return (int) ((baseManaCost + manaCostPerLevel * (level - 1)) * manaMultiplier.orElse(1d));
    }

    public int getSpellCooldown() {
        return this.cooldown.orElse(200);
    }

    private int getCastTime() {
        return this.castTime;
    }

    public CastDataSerializable getEmptyCastData() {
        return null;
    }

    public abstract Optional<SoundEvent> getCastStartSound();

    public abstract Optional<SoundEvent> getCastFinishSound();

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public Either<AnimationBuilder, ResourceLocation> getCastStartAnimation(Player player) {
        return switch (this.castType) {
            case INSTANT -> player == null ? Either.left(ANIMATION_INSTANT_CAST) : Either.right(ANIMATION_INSTANT_CAST_RESOURCE);
            case CONTINUOUS -> player == null ? Either.left(ANIMATION_CONTINUOUS_CAST) : Either.right(ANIMATION_CONTINUOUS_CAST_RESOURCE);
            case LONG -> player == null ? Either.left(ANIMATION_LONG_CAST) : Either.right(ANIMATION_LONG_CAST_RESOURCE);
            case CHARGE -> player == null ? Either.left(ANIMATION_CHARGED_CAST) : Either.right(ANIMATION_CHARGED_CAST_RESOURCE);
            default -> Either.left(null);
        };
    }

    /**
     * Default Animations Based on Cast Type. Override for specific spell-based animations
     */
    public Either<AnimationBuilder, ResourceLocation> getCastFinishAnimation(Player player) {
        return switch (this.castType) {
            case LONG -> player == null ? Either.left(ANIMATION_LONG_CAST_FINISH) : Either.right(ANIMATION_LONG_CAST_FINISH_RESOURCE);
            default -> Either.left(null);
        };
    }

    public float getSpellPower(@Nullable Entity sourceEntity) {

        float entitySpellPowerModifier = 1;
        float entitySchoolPowerModifier = 1;
        float configPowerModifier = (powerMultiplier.orElse(1d)).floatValue();
        if (sourceEntity instanceof LivingEntity sourceLivingEntity) {
            IronsSpellbooks.LOGGER.debug("AbsSpell.getSpellPower: \"use item\": {}", sourceLivingEntity.getUseItem());
            entitySpellPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER.get());
            switch (this.getSchoolType()) {
                case FIRE ->
                        entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER.get());
                case ICE ->
                        entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.ICE_SPELL_POWER.get());
                case LIGHTNING ->
                        entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get());
                case HOLY ->
                        entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.HOLY_SPELL_POWER.get());
                case ENDER ->
                        entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.ENDER_SPELL_POWER.get());
                case BLOOD ->
                        entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.BLOOD_SPELL_POWER.get());
                case EVOCATION ->
                        entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.EVOCATION_SPELL_POWER.get());
            }

        }

        return (baseSpellPower + spellPowerPerLevel * (level - 1)) * entitySpellPowerModifier * entitySchoolPowerModifier * configPowerModifier;
    }

    public int getEffectiveCastTime(@Nullable LivingEntity entity) {
        double entityCastTimeModifier = 1;
        if(entity !=null){
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
        IronsSpellbooks.LOGGER.debug("attemptInitiateCast spell:{} **********************************************************************************", this.getSpellType());
        if (level.isClientSide) {
            return false;
        }

        var serverPlayer = (ServerPlayer) player;
        var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);

        if (!playerMagicData.isCasting()) {
            int playerMana = playerMagicData.getMana();

            boolean hasEnoughMana = playerMana - getManaCost() >= 0;
            boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(spellType);

            if ((castSource == CastSource.SPELLBOOK || castSource == CastSource.SWORD) && isSpellOnCooldown) {
                Messages.sendToPlayer(new ClientboundCastError(ClientboundCastError.CastErrorMessages.COOLDOWN.id, this.spellType.getValue()), serverPlayer);
                return false;
            }

            if (castSource.consumesMana() && !hasEnoughMana) {
                Messages.sendToPlayer(new ClientboundCastError(ClientboundCastError.CastErrorMessages.MANA.id, this.spellType.getValue()), serverPlayer);
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
                //TODO: effective cast time needs better logic (it reduces continuous cast duration and will need to be utilized in faster charge casting)
                /*
                 * Prepare to cast spell (magic manager will pick it up by itself)
                 */
                int effectiveCastTime = getEffectiveCastTime(player);
                playerMagicData.initiateCast(getID(), this.level, effectiveCastTime, castSource);
                onServerPreCast(player.level, player, playerMagicData);
                Messages.sendToPlayer(new ClientboundUpdateCastingState(getID(), getLevel(), effectiveCastTime, castSource), serverPlayer);
            }

            Messages.sendToPlayersTrackingEntity(new ClientboundOnCastStarted(serverPlayer.getUUID(), spellType), serverPlayer, true);

            return true;
        } else {
            Utils.serverSideCancelCast(serverPlayer);
            return false;
        }
    }

    public void castSpell(Level world, ServerPlayer serverPlayer, CastSource castSource, boolean triggerCooldown) {
        MagicManager magicManager = MagicManager.get(serverPlayer.level);
        PlayerMagicData playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);

        if (castSource.consumesMana()) {
            //TODO: sword mana multiplier?
            int newMana = playerMagicData.getMana() - getManaCost();
            magicManager.setPlayerCurrentMana(serverPlayer, newMana);
            Messages.sendToPlayer(new ClientboundSyncMana(playerMagicData), serverPlayer);
        }

        if (triggerCooldown) {
            MagicManager.get(serverPlayer.level).addCooldown(serverPlayer, spellType, castSource);
        }

        onCast(world, serverPlayer, playerMagicData);
        Messages.sendToPlayer(new ClientboundOnClientCast(this.getID(), this.level, castSource, playerMagicData.getAdditionalCastData()), serverPlayer);

        if (this.castType == CastType.INSTANT) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.castSpell -> onServerCastComplete (not continuous)");
            onServerCastComplete(world, serverPlayer, playerMagicData, false);
        }

        if (serverPlayer.getMainHandItem().getItem() instanceof SpellBook || serverPlayer.getMainHandItem().getItem() instanceof Scroll)
            playerMagicData.setPlayerCastingItem(serverPlayer.getMainHandItem());
        else
            playerMagicData.setPlayerCastingItem(serverPlayer.getOffhandItem());

    }

//    private int getCooldownLength(ServerPlayer serverPlayer) {
//        double playerCooldownModifier = serverPlayer.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION.get());
//        return MagicManager.getEffectiveSpellCooldown(cooldown, playerCooldownModifier);
//    }

    /**
     * The primary spell effect sound and particle handling goes here. Called Client Side only
     */
    public void onClientCast(Level level, LivingEntity entity, CastData castData) {
        IronsSpellbooks.LOGGER.debug("AbstractSpell.onClientCastComplete.1");
        playSound(getCastFinishSound(), entity, true);
        if (ClientInputEvents.isUseKeyDown) {
            IronsSpellbooks.LOGGER.debug("AbstractSpell.onClientCastComplete.2");
            if (this.spellType.getCastType().holdToCast()) {
                ClientSpellCastHelper.setSuppressRightClicks(true);
                IronsSpellbooks.LOGGER.debug("AbstractSpell.onClientCastComplete.3");
            }
            ClientInputEvents.hasReleasedSinceCasting = false;
        }
    }

    /**
     * The primary spell effect handling goes here. Called Server Side
     */
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        playSound(getCastFinishSound(), entity, true);
        //IronsSpellbooks.LOGGER.debug("Yrot:{},BodyYRot:{},HeadYRot:{}", entity.getYRot(), entity.yBodyRot, entity.yHeadRot);
    }

    protected void playSound(Optional<SoundEvent> sound, Entity entity, boolean playDefaultSound) {
        //IronsSpellbooks.LOGGER.debug("playSound spell:{} isClientSide:{}", this.getSpellType(), entity.level.isClientSide);
        // sound.ifPresent((soundEvent) -> entity.playSound(soundEvent, 1.0f, 1.0f));

        if (sound.isPresent()) {
            IronsSpellbooks.LOGGER.debug("playSound spell:{} isClientSide:{}, resourceLocation:{}", this.getSpellType(), entity.level.isClientSide, sound.get().getLocation().toString());
            entity.playSound(sound.get(), 2.0f, 1.0f);
        } else if (playDefaultSound) {
            entity.playSound(defaultCastSound(), 2.0f, 1.0f);
        }

        //entity.playSound(sound.orElse(this:def), 1.0f, 1.0f));
    }

    /**
     * Server Side. Used to see if a spell is allowed to be cast, such as if it requires a target but finds none
     */
    public boolean checkPreCastConditions(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
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
            default -> SoundRegistry.EVOCATION_CAST.get();
        };
    }

    /**
     * Called on the server when a spell finishes casting or is cancelled, used for any cleanup or extra functionality
     */
    public void onServerCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData, boolean cancelled) {
        IronsSpellbooks.LOGGER.debug("AbstractSpell.onServerCastComplete");
        playerMagicData.resetCastingState();
        if (entity instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayersTrackingEntity(new ClientboundOnCastFinished(serverPlayer.getUUID(), spellType, cancelled), serverPlayer, true);
        }
    }

    /**
     * Called once just before executing onCast. Can be used for client side sounds and particles
     */
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        //irons_spellbooks.LOGGER.debug("AbstractSpell.onClientPreCast: isClient:{} entity:{}", level.isClientSide, entity);
        playSound(getCastStartSound(), entity);
    }

    /**
     * Called once just before executing onCast. Can be used for server side sounds and particles
     */
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        //irons_spellbooks.LOGGER.debug("AbstractSpell.: onServerPreCast:{}", level.isClientSide);
        playSound(getCastStartSound(), entity);
    }

    /**
     * Called on the server each tick while casting
     */
    public void onServerCastTick(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {

    }

    /**
     * Used by AbstractSpellCastingMob to determine if the cast is no longer valid (ie player out of range of a particular spell). Override to create spell-specific criteria
     */
    public boolean shouldAIStopCasting(AbstractSpellCastingMob mob, LivingEntity target) {
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
