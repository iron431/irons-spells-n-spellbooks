package io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class VaultSharedData {
    static final String TAG_NAME = "shared_data";
    static Codec<VaultSharedData> CODEC = RecordCodecBuilder.create(
            p_338074_ -> p_338074_.group(
                            ItemStack.CODEC.optionalFieldOf("display_item", ItemStack.EMPTY).forGetter(p_324217_ -> p_324217_.displayItem),
                            VaultServerData.UUID_SET.optionalFieldOf("connected_players", Set.of()).forGetter(p_324110_ -> p_324110_.connectedPlayers),
                            Codec.DOUBLE
                                    .optionalFieldOf("connected_particles_range", Double.valueOf(VaultConfig.DEFAULT.deactivationRange()))
                                    .forGetter(p_323486_ -> p_323486_.connectedParticlesRange)
                    )
                    .apply(p_338074_, VaultSharedData::new)
    );
    private ItemStack displayItem = ItemStack.EMPTY;
    private Set<UUID> connectedPlayers = new ObjectLinkedOpenHashSet<>();
    private double connectedParticlesRange = VaultConfig.DEFAULT.deactivationRange();
    boolean isDirty;

    VaultSharedData(ItemStack displayItem, Set<UUID> connectedPlayers, double connectedParticlesRange) {
        this.displayItem = displayItem;
        this.connectedPlayers.addAll(connectedPlayers);
        this.connectedParticlesRange = connectedParticlesRange;
    }

    VaultSharedData() {
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public boolean hasDisplayItem() {
        return !this.displayItem.isEmpty();
    }

    public void setDisplayItem(ItemStack displayItem) {
        if (!ItemStack.matches(this.displayItem, displayItem)) {
            this.displayItem = displayItem.copy();
            this.markDirty();
        }
    }

    boolean hasConnectedPlayers() {
        return !this.connectedPlayers.isEmpty();
    }

    Set<UUID> getConnectedPlayers() {
        return this.connectedPlayers;
    }

    double connectedParticlesRange() {
        return this.connectedParticlesRange;
    }

    void updateConnectedPlayersWithinRange(ServerLevel level, BlockPos pos, VaultServerData serverData, VaultConfig config, double deactivationRange) {
        Set<UUID> set = config.playerDetector()
                .detect(level, config.entitySelector(), pos, deactivationRange, false)
                .stream()
                .filter(p_324308_ -> !serverData.getRewardedPlayers().contains(p_324308_))
                .collect(Collectors.toSet());
        if (!this.connectedPlayers.equals(set)) {
            this.connectedPlayers = set;
            this.markDirty();
        }
    }

    private void markDirty() {
        this.isDirty = true;
    }

    void set(VaultSharedData other) {
        this.displayItem = other.displayItem;
        this.connectedPlayers = other.connectedPlayers;
        this.connectedParticlesRange = other.connectedParticlesRange;
    }
}
