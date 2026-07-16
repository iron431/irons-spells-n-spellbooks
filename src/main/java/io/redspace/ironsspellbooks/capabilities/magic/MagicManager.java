package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.skillcasting.data.cast.CasterRef;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MANA_REGEN;
import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;

public class MagicManager {
    public static final int MANA_REGEN_TICKS = 10;
    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;

    public boolean regenPlayerMana(ServerPlayer serverPlayer, MagicData playerMagicData) {
        int playerMaxMana = (int) serverPlayer.getAttributeValue(MAX_MANA);
        var mana = playerMagicData.getMana();
        if (mana != playerMaxMana) {
            float playerManaRegenMultiplier = (float) serverPlayer.getAttributeValue(MANA_REGEN);
            var increment = playerMaxMana * playerManaRegenMultiplier * .01f * ServerConfigs.MANA_REGEN_MULTIPLIER.get().floatValue();
            playerMagicData.setMana(Mth.clamp(playerMagicData.getMana() + increment, 0, playerMaxMana));
            return true;
        } else {
            return false;
        }
    }


    public void tick(Level level) {
        boolean doManaRegen = level.getServer().getTickCount() % MANA_REGEN_TICKS == 0;
        if (doManaRegen) {
            level.players().stream().toList().forEach(player -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    MagicData playerMagicData = MagicData.get(serverPlayer);
                    if (regenPlayerMana(serverPlayer, playerMagicData)) {
                        SyncManaPacket.syncFor(CasterRef.entity(serverPlayer));
                    }
                }
            });
        }
    }

    public static void spawnParticles(Level level, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean force) {
        level.getServer().getPlayerList().getPlayers().forEach(player -> ((ServerLevel) level).sendParticles(player, particle, force, x, y, z, count, deltaX, deltaY, deltaZ, speed));
    }
}
