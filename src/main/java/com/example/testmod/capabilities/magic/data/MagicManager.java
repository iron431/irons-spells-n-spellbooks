package com.example.testmod.capabilities.magic.data;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.network.PacketCastingState;
import com.example.testmod.capabilities.magic.network.PacketSyncCooldownToClient;
import com.example.testmod.capabilities.magic.network.PacketSyncManaToClient;
import com.example.testmod.item.Scroll;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import static com.example.testmod.registries.AttributeRegistry.COOLDOWN_REDUCTION;
import static com.example.testmod.registries.AttributeRegistry.MAX_MANA;

public class MagicManager {

    public static final int TICKS_PER_CYCLE = 20;
    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;
    private static MagicManager magicManager = null;

    private int counter = 0;

    @Nonnull
    public static MagicManager get(Level level) {

        if (level.isClientSide) {
            throw new RuntimeException("Don't access the ManaManager client-side!");
        }

        if (magicManager == null) {
            magicManager = new MagicManager();
        }
        return magicManager;
    }

    public PlayerMagicData getPlayerMagicData(ServerPlayer serverPlayer) {
        var capContainer = serverPlayer.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (capContainer.isPresent()) {
            return capContainer.resolve().orElse(new PlayerMagicData());
        }
        return new PlayerMagicData();
    }

    public int getPlayerCurrentMana(ServerPlayer serverPlayer) {
        return getPlayerMagicData(serverPlayer).getMana();
    }

    public void setPlayerCurrentMana(ServerPlayer serverPlayer, int newManaValue) {
        var playerMagicData = getPlayerMagicData(serverPlayer);
        playerMagicData.setMana(newManaValue);
    }



    public void regenPlayerMana(ServerPlayer serverPlayer, PlayerMagicData playerMagicData) {
        int playerMaxMana = (int) serverPlayer.getAttributeValue(MAX_MANA.get());
        int increment = Math.round(Math.max(playerMaxMana * .01f, 1));

        if (playerMagicData.getMana() != playerMaxMana) {
            if (playerMagicData.getMana() + increment < playerMaxMana) {
                playerMagicData.addMana(increment);
            } else {
                playerMagicData.setMana(playerMaxMana);
            }
        }
    }

    public void tick(Level level) {
        counter--;

        level.players().forEach(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerMagicData playerMagicData = getPlayerMagicData(serverPlayer);

                playerMagicData.getPlayerCooldowns().tick(1);

                if (playerMagicData.isCasting()) {
                    var spell = AbstractSpell.getSpell(playerMagicData.getCastingSpellId(), playerMagicData.getCastingSpellLevel());
                    playerMagicData.handleCastDuration();
                    if (spell.getCastType() == CastType.LONG) {
                        if (!playerMagicData.isCasting()) {
                            TestMod.LOGGER.info("MagicManager.tick: handle spell casting complete");
                            Messages.sendToPlayer(new PacketCastingState(playerMagicData.getCastingSpellId(), 0, spell.getCastType(), true), serverPlayer);
                            spell.castSpell(serverPlayer.level, serverPlayer, true, true);
                            playerMagicData.resetCastingState();
                            Scroll.attemptRemoveScrollAfterCast(serverPlayer);
                        }
                    } else if (spell.getCastType() == CastType.CONTINUOUS) {
                        if ((playerMagicData.getCastDurationRemaining() + 1) % CONTINUOUS_CAST_TICK_INTERVAL == 0) {
                            if (playerMagicData.getCastDurationRemaining() < CONTINUOUS_CAST_TICK_INTERVAL || playerMagicData.getMana() - spell.getManaCost() * 2 < 0) {
                                TestMod.LOGGER.info("MagicManager.tick: handle spell casting complete");
                                Messages.sendToPlayer(new PacketCastingState(playerMagicData.getCastingSpellId(), 0, spell.getCastType(), true), serverPlayer);
                                spell.castSpell(serverPlayer.level, serverPlayer, true, true);
                                playerMagicData.resetCastingState();
                                Scroll.attemptRemoveScrollAfterCast(serverPlayer);
                            } else {
                                spell.castSpell(serverPlayer.level, serverPlayer, true, false);
                            }

                        }
                    }
                }

                if (counter <= 0) {
                    counter = TICKS_PER_CYCLE;
                    regenPlayerMana(serverPlayer, playerMagicData);
                    Messages.sendToPlayer(new PacketSyncManaToClient(playerMagicData), serverPlayer);
                }
            }
        });
    }

    public MagicManager() {
    }

    public void addCooldown(ServerPlayer serverPlayer, SpellType spellType) {
        double playerCooldownModifier = serverPlayer.getAttributeValue(COOLDOWN_REDUCTION.get());
        int effectiveCooldown = getEffectiveSpellCooldown(AbstractSpell.getSpell(spellType, 1).getSpellCooldown(), playerCooldownModifier);
        getPlayerMagicData(serverPlayer).getPlayerCooldowns().addCooldown(spellType, effectiveCooldown);
        Messages.sendToPlayer(new PacketSyncCooldownToClient(spellType.getValue(), effectiveCooldown), serverPlayer);
    }

    public static int getEffectiveSpellCooldown(int cooldown, double playerCooldownModifier) {
        return (int) (cooldown * (2 - playerCooldownModifier));
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double radiusX, double radiusY, double radiusZ, double speed, boolean force) {
        ((ServerLevel) level).sendParticles(particle, x, y, z, count, radiusX, radiusY, radiusZ, speed);
    }

}
