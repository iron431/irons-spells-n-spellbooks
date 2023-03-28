package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.network.ClientboundSyncCooldown;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import static io.redspace.ironsspellbooks.registries.AttributeRegistry.COOLDOWN_REDUCTION;
import static io.redspace.ironsspellbooks.registries.AttributeRegistry.MAX_MANA;

public class MagicManager {

    public static final int TICKS_PER_CYCLE = 10;
    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;

    private int counter = 0;
    private static MagicManager magicManager = null;

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


    public void setPlayerCurrentMana(ServerPlayer serverPlayer, int newManaValue) {
        var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
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
                PlayerMagicData playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
                playerMagicData.getPlayerCooldowns().tick(1);

                if (playerMagicData.isCasting()) {
                    playerMagicData.handleCastDuration();
                    var spell = AbstractSpell.getSpell(playerMagicData.getCastingSpellId(), playerMagicData.getCastingSpellLevel());
                    //irons_spellbooks.LOGGER.debug("MagicManager.tick: playerMagicData:{}", playerMagicData);
                    if (spell.getCastType() == CastType.LONG || (spell.getCastType() == CastType.CHARGE && !serverPlayer.isUsingItem())) {
                        if (playerMagicData.getCastDurationRemaining() <= 0) {
                            //Messages.sendToPlayer(new ClientboundUpdateCastingState(playerMagicData.getCastingSpellId(), 0, 0, playerMagicData.getCastSource(), true), serverPlayer);
                            spell.castSpell(serverPlayer.level, serverPlayer, playerMagicData.getCastSource(), true);
                            IronsSpellbooks.LOGGER.debug("MagicManager.tick.1");
                            spell.onServerCastComplete(serverPlayer.level, serverPlayer, playerMagicData);
                            Scroll.attemptRemoveScrollAfterCast(serverPlayer);
                        }
                    } else if (spell.getCastType() == CastType.CONTINUOUS) {
                        if ((playerMagicData.getCastDurationRemaining() + 1) % CONTINUOUS_CAST_TICK_INTERVAL == 0) {
                            if (playerMagicData.getCastDurationRemaining() < CONTINUOUS_CAST_TICK_INTERVAL || (playerMagicData.getCastSource().consumesMana() && playerMagicData.getMana() - spell.getManaCost() * 2 < 0)) {
                                //irons_spellbooks.LOGGER.debug("MagicManager.tick: handle spell casting complete");
                                //Messages.sendToPlayer(new ClientboundUpdateCastingState(playerMagicData.getCastingSpellId(), 0, 0, playerMagicData.getCastSource(), true), serverPlayer);
                                spell.castSpell(serverPlayer.level, serverPlayer, playerMagicData.getCastSource(), true);
                                spell.onServerCastComplete(serverPlayer.level, serverPlayer, playerMagicData);
                                IronsSpellbooks.LOGGER.debug("MagicManager.tick.2");
                                if (playerMagicData.getCastSource() == CastSource.SCROLL) {
                                    Scroll.attemptRemoveScrollAfterCast(serverPlayer);
                                }
                            } else {
                                spell.castSpell(serverPlayer.level, serverPlayer, playerMagicData.getCastSource(), false);
                            }
                        }
                    }

                    if (playerMagicData.isCasting()) {
                        spell.onServerCastTick(serverPlayer.level, serverPlayer, playerMagicData);
                    }
                }

                if (counter <= 0) {
                    regenPlayerMana(serverPlayer, playerMagicData);
                    Messages.sendToPlayer(new ClientboundSyncMana(playerMagicData), serverPlayer);
                }
            }
        });

        if (counter <= 0) {
            counter = TICKS_PER_CYCLE;
        }
    }

    public void addCooldown(ServerPlayer serverPlayer, SpellType spellType, CastSource castSource) {
        if (castSource == CastSource.SCROLL)
            return;
        int effectiveCooldown = getEffectiveSpellCooldown(spellType, serverPlayer, castSource);

        //IronsSpellbooks.LOGGER.debug("addCooldown: serverPlayer: {} playerCooldownModifier:{} effectiveCooldown:{}", serverPlayer.getName().getString(), playerCooldownModifier, effectiveCooldown);

        PlayerMagicData.getPlayerMagicData(serverPlayer).getPlayerCooldowns().addCooldown(spellType, effectiveCooldown);
        Messages.sendToPlayer(new ClientboundSyncCooldown(spellType.getValue(), effectiveCooldown), serverPlayer);
    }


    public static int getEffectiveSpellCooldown(SpellType spellType, Player player, CastSource castSource) {
        double playerCooldownModifier = player.getAttributeValue(COOLDOWN_REDUCTION.get());

        int itemCoolDownModifer = 1;
        if (castSource == CastSource.SWORD) {
            itemCoolDownModifer = 2;
        }

        return (int) (AbstractSpell.getSpell(spellType, 1).getSpellCooldown() * (2 - playerCooldownModifier) * itemCoolDownModifer);
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }

//    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double radiusX, double radiusY, double radiusZ, double speed, boolean force) {
//        ((ServerLevel) level).sendParticles(particle, x, y, z, count, radiusX, radiusY, radiusZ, speed);
//    }

}
