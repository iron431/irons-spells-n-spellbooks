package com.example.testmod.spells;

import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.network.PacketCastingState;
import com.example.testmod.network.PacketSyncManaToClient;
import com.example.testmod.player.ServerPlayerEvents;
import com.example.testmod.registries.AttributeRegistry;
import com.example.testmod.setup.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

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

    public int getLevel() {
        return this.level;
    }

    public int getManaCost() {
        return baseManaCost + manaCostPerLevel * (level - 1);
    }

    public float getSpellPower(Entity sourceEntity) {
        float entitySpellPowerModifer = 1;
        if (sourceEntity instanceof LivingEntity sourceLivingEntity) {
            entitySpellPowerModifer = (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER.get());
        }

        return (baseSpellPower + spellPowerPerLevel * (level - 1)) * entitySpellPowerModifer;
    }

    public int getSpellCooldown() {
        return this.cooldown;
    }

    public int getCastTime() {
        return this.castTime;
    }

    public int getEffectiveCastTime(Entity sourceEntity) {
        float entityCastTimeModifer = 1;
        if (sourceEntity instanceof LivingEntity sourceLivingEntity) {
            entityCastTimeModifer = 2 - (float) sourceLivingEntity.getAttributeValue(AttributeRegistry.CAST_TIME_REDUCTION.get());
        }

        return Math.round(this.castTime * entityCastTimeModifer);
    }

    public void setLevel(int level) {
        this.level = level;
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
    public boolean attemptInitiateCast(ItemStack stack, Level level, Player player, boolean fromScroll, boolean triggerCooldown) {
        if (level.isClientSide) {
            return false;
        }

        var serverPlayer = (ServerPlayer) player;
        var playerMagicData = MagicManager.getPlayerMagicData(serverPlayer);

        if (!playerMagicData.isCasting()) {
            int playerMana = playerMagicData.getMana();

            boolean hasEnoughMana = playerMana - getManaCost() >= 0;
            boolean isSpellOnCooldown = playerMagicData.getPlayerCooldowns().isOnCooldown(spellType);

            if (!fromScroll) {
                if (!hasEnoughMana) {
                    player.sendSystemMessage(Component.literal("Not enough mana to cast spell").withStyle(ChatFormatting.RED));
                    return false;
                }
                if (isSpellOnCooldown) {
                    player.sendSystemMessage(spellType.getDisplayName().append(" is on cooldown").withStyle(ChatFormatting.RED));
                    return false;
                }
            }

            if (this.castType == CastType.INSTANT) {
                return castSpell(level, serverPlayer, fromScroll, triggerCooldown);
            } else if (this.castType == CastType.LONG || this.castType == CastType.CONTINUOUS) {
                int effectiveCastTime = getEffectiveCastTime(player);
                playerMagicData.initiateCast(getID(), this.level, effectiveCastTime, fromScroll);
                onServerPreCast(player.level, player, playerMagicData);
                Messages.sendToPlayer(new PacketCastingState(getID(), effectiveCastTime, castType, false), serverPlayer);
            }
            return true;
        } else {
            ServerPlayerEvents.serverSideCancelCast(serverPlayer, playerMagicData);
            return false;
        }
    }

    public boolean castSpell(Level world, ServerPlayer serverPlayer, boolean fromScroll, boolean triggerCooldown) {
        MagicManager magicManager = MagicManager.get(serverPlayer.level);
        PlayerMagicData playerMagicData = MagicManager.getPlayerMagicData(serverPlayer);

        if (!fromScroll) {
            int newMana = playerMagicData.getMana() - getManaCost();
            magicManager.setPlayerCurrentMana(serverPlayer, newMana);
        }

        if (triggerCooldown) {
            MagicManager.get(serverPlayer.level).addCooldown(serverPlayer, spellType);
        }

        onCast(world, serverPlayer, playerMagicData);

        if (this.castType != CastType.CONTINUOUS) {
            onCastComplete(world, serverPlayer, playerMagicData);
        }

        if (!fromScroll) {
            Messages.sendToPlayer(new PacketSyncManaToClient(playerMagicData), serverPlayer);
        }

        if (serverPlayer.getMainHandItem().getItem() instanceof SpellBook || serverPlayer.getMainHandItem().getItem() instanceof Scroll)
            playerMagicData.setPlayerCastingItem(serverPlayer.getMainHandItem());
        else
            playerMagicData.setPlayerCastingItem(serverPlayer.getOffhandItem());

        return true;
    }

    private int getCooldownLength(ServerPlayer serverPlayer) {
        double playerCooldownModifier = serverPlayer.getAttributeValue(COOLDOWN_REDUCTION.get());
        return MagicManager.getEffectiveSpellCooldown(cooldown, playerCooldownModifier);
    }

    /**
     * The primary spell effect handling goes here
     */
    public abstract void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData);

    /**
     * Called on the server when a long spell casts, or a continuous spell is done casting or cancelled
     */
    public void onCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
    }

    /**
     * Called once just before executing onCast. Can be used for client side sounds and particles
     */
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
    }

    /**
     * Called once just before executing onCast. Can be used for server side sounds and particles
     */
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
    }

    /**
     * Called on the server each tick while casting LONG and CONTINUOUS only
     */
    public void onServerCastTick(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
    }

    public MutableComponent getUniqueInfo() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (o == null)
            return false;
        return this.spellType == o.spellType && this.level == o.level;
    }
}
