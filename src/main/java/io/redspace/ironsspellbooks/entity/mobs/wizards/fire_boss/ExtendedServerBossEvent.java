package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class ExtendedServerBossEvent extends BossEvent {
    private final Set<ServerPlayer> players = Sets.newHashSet();
    private final Set<ServerPlayer> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
    private boolean visible = true;

    public ExtendedServerBossEvent(UUID uuid, Component name, BossBarColor color, BossBarOverlay overlay) {
        super(uuid, name, color, overlay);
    }

    @Override
    public void setProgress(float progress) {
        if (progress != this.progress) {
            super.setProgress(progress);
            this.broadcast(ClientboundBossEventPacket::createUpdateProgressPacket);
        }
    }

    @Override
    public void setColor(BossBarColor color) {
        if (color != this.color) {
            super.setColor(color);
            this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
        }
    }

    @Override
    public void setOverlay(BossBarOverlay overlay) {
        if (overlay != this.overlay) {
            super.setOverlay(overlay);
            this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
        }
    }

    @Override
    public BossEvent setDarkenScreen(boolean darkenSky) {
        if (darkenSky != this.darkenScreen) {
            super.setDarkenScreen(darkenSky);
            this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    @Override
    public BossEvent setPlayBossMusic(boolean playEndBossMusic) {
        if (playEndBossMusic != this.playBossMusic) {
            super.setPlayBossMusic(playEndBossMusic);
            this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    @Override
    public BossEvent setCreateWorldFog(boolean createFog) {
        if (createFog != this.createWorldFog) {
            super.setCreateWorldFog(createFog);
            this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
        }

        return this;
    }

    @Override
    public void setName(Component name) {
        if (!Objects.equal(name, this.name)) {
            super.setName(name);
            this.broadcast(ClientboundBossEventPacket::createUpdateNamePacket);
        }
    }

    private void broadcast(Function<BossEvent, ClientboundBossEventPacket> packetGetter) {
        if (this.visible) {
            ClientboundBossEventPacket clientboundbosseventpacket = packetGetter.apply(this);

            for (ServerPlayer serverplayer : this.players) {
                serverplayer.connection.send(clientboundbosseventpacket);
            }
        }
    }

    /**
     * Makes the boss visible to the given player.
     */
    public void addPlayer(ServerPlayer player) {
        if (this.players.add(player) && this.visible) {
            player.connection.send(ClientboundBossEventPacket.createAddPacket(this));
        }
    }

    /**
     * Makes the boss non-visible to the given player.
     */
    public void removePlayer(ServerPlayer player) {
        if (this.players.remove(player) && this.visible) {
            player.connection.send(ClientboundBossEventPacket.createRemovePacket(this.getId()));
        }
    }

    public void removeAllPlayers() {
        if (!this.players.isEmpty()) {
            for (ServerPlayer serverplayer : Lists.newArrayList(this.players)) {
                this.removePlayer(serverplayer);
            }
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;

            for (ServerPlayer serverplayer : this.players) {
                serverplayer.connection
                    .send(visible ? ClientboundBossEventPacket.createAddPacket(this) : ClientboundBossEventPacket.createRemovePacket(this.getId()));
            }
        }
    }

    public Collection<ServerPlayer> getPlayers() {
        return this.unmodifiablePlayers;
    }
}
