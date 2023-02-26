package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.network.ClientboundOnClientCast;
import com.example.testmod.network.ClientboundSyncMana;
import com.example.testmod.network.ClientboundUpdateCastingState;
import com.example.testmod.player.ClientInputEvents;
import com.example.testmod.player.ClientRenderCache;
import com.example.testmod.registries.AttributeRegistry;
import com.example.testmod.setup.Messages;
import com.example.testmod.util.Utils;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.testmod.registries.AttributeRegistry.COOLDOWN_REDUCTION;

public abstract class AbstractSpell {
    private final SpellType spellType;
    private final CastType castType;
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    //All time values in ticks
    protected int castTime;
    protected int cooldown;

    protected final List<MutableComponent> uniqueInfo = new ArrayList<>();

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
        return spellType.getRarity(level);
    }

    public CastType getCastType() {
        return this.castType;
    }

    public SchoolType getSchoolType() {
        return spellType.getSchoolType();
    }

    public DamageSource getDamageSource() {
        return this.spellType.getDamageSource();
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getManaCost() {
        return baseManaCost + manaCostPerLevel * (level - 1);
    }

    public int getSpellCooldown() {
        return this.cooldown;
    }

    public int getCastTime() {
        return this.castTime;
    }

    public abstract Optional<SoundEvent> getCastStartSound();

    public Optional<ModifierLayer<IAnimation>> getCastStartAnimation() {
        return Optional.empty();
    }

    public abstract Optional<SoundEvent> getCastFinishSound();

    public float getSpellPower(Entity sourceEntity) {
        float entitySpellPowerModifier = 1;
        float entitySchoolPowerModifier = 1;
        if (sourceEntity instanceof LivingEntity sourceLivingEntity) {
            entitySpellPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER.get());
            switch (this.getSchoolType()) {
                case FIRE -> entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.FIRE_SPELL_POWER.get());
                case ICE -> entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.ICE_SPELL_POWER.get());
                case LIGHTNING -> entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get());
                case HOLY -> entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.HOLY_SPELL_POWER.get());
                case ENDER -> entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.ENDER_SPELL_POWER.get());
                case BLOOD -> entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.BLOOD_SPELL_POWER.get());
                case EVOCATION -> entitySchoolPowerModifier = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.EVOCATION_SPELL_POWER.get());
            }
        }

        return (baseSpellPower + spellPowerPerLevel * (level - 1)) * entitySpellPowerModifier * entitySchoolPowerModifier;
    }

    public int getEffectiveCastTime(Entity sourceEntity) {
        float entityCastTimeModifer = 1;
        if (sourceEntity instanceof LivingEntity sourceLivingEntity) {
            entityCastTimeModifer = 2 - (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION.get());
        }

        return Math.round(this.castTime * entityCastTimeModifer);
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
        if (level.isClientSide) {
            return false;
        }

        var serverPlayer = (ServerPlayer) player;
        var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);

        if (!playerMagicData.isCasting()) {
            int playerMana = playerMagicData.getMana();

            boolean hasEnoughMana = playerMana - getManaCost() >= 0;
            boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(spellType);

            if (castSource.consumesMana() && !hasEnoughMana) {
                player.sendSystemMessage(Component.literal("Not enough mana to cast spell").withStyle(ChatFormatting.RED));
                return false;
            }

            if ((castSource == CastSource.SPELLBOOK || castSource == CastSource.SWORD) && isSpellOnCooldown) {
                player.sendSystemMessage(spellType.getDisplayName().append(" is on cooldown").withStyle(ChatFormatting.RED));
                return false;
            }

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
                Messages.sendToPlayer(new ClientboundUpdateCastingState(getID(), getLevel(), effectiveCastTime, castSource, false), serverPlayer);
            }

            //Messages

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

        Messages.sendToPlayer(new ClientboundOnClientCast(this.getID(), this.level, castSource), serverPlayer);

        onCast(world, serverPlayer, playerMagicData);

        if (this.castType != CastType.CONTINUOUS) {
            onServerCastComplete(world, serverPlayer, playerMagicData);
        }

        if (serverPlayer.getMainHandItem().getItem() instanceof SpellBook || serverPlayer.getMainHandItem().getItem() instanceof Scroll)
            playerMagicData.setPlayerCastingItem(serverPlayer.getMainHandItem());
        else
            playerMagicData.setPlayerCastingItem(serverPlayer.getOffhandItem());

    }

    private int getCooldownLength(ServerPlayer serverPlayer) {
        double playerCooldownModifier = serverPlayer.getAttributeValue(COOLDOWN_REDUCTION.get());
        return MagicManager.getEffectiveSpellCooldown(cooldown, playerCooldownModifier);
    }

    /**
     * The primary spell effect sound and particle handling goes here. Called Client Side only
     */
    public void onClientCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        //TestMod.LOGGER.debug("AbstractSpell.: onClientCast:{}", level.isClientSide);
        playSound(getCastFinishSound(), entity);
        if (ClientInputEvents.isUseKeyDown) {
            ClientRenderCache.setSuppressRightClicks(true);
        }
    }

    /**
     * The primary spell effect handling goes here. Called Server Side
     */
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        playSound(getCastFinishSound(), entity);
    }

    private void playSound(Optional<SoundEvent> sound, Entity entity) {
        sound.ifPresent((soundEvent) -> entity.playSound(soundEvent, 1.0f, 1.0f));
    }

    /**
     * Called on the server when a spell finishes casting or is cancelled, used for any cleanup or extra functionality
     */
    public void onServerCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
    }

    /**
     * Called once just before executing onCast. Can be used for client side sounds and particles
     */
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        TestMod.LOGGER.debug("AbstractSpell.onClientPreCast: isClient:{} entity:{}", level.isClientSide, entity);

        //Get the animation for that player
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) entity).get(new ResourceLocation(TestMod.MODID, "animation"));
        if (animation != null) {
            //You can set an animation from anywhere ON THE CLIENT
            //Do not attempt to do this on a server, that will only fail

            animation.setAnimation(new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation(TestMod.MODID, "instant_cast"))));
            //You might use  animation.replaceAnimationWithFade(); to create fade effect instead of sudden change
            //See javadoc for details
        }


        playSound(getCastStartSound(), entity);
    }

    /**
     * Called once just before executing onCast. Can be used for server side sounds and particles
     */
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        //TestMod.LOGGER.debug("AbstractSpell.: onServerPreCast:{}", level.isClientSide);
        playSound(getCastStartSound(), entity);
    }

    /**
     * Called on the server each tick while casting
     */
    public void onServerCastTick(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {

    }

    public List<MutableComponent> getUniqueInfo() {
        return uniqueInfo;
    }

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (o == null)
            return false;
        return this.spellType == o.spellType && this.level == o.level;
    }
}
