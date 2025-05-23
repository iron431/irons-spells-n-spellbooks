package io.redspace.ironsspellbooks.entity.spells.pocket_dimension_portal;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public class PocketDimensionIdManager implements INBTSerializable<CompoundTag> {
    public static final ResourceKey<Level> POCKET_DIMENSION = ResourceKey.create(Registries.DIMENSION, IronsSpellbooks.id("pocket_dimension"));

    private static final String UUID_KEY = "uuid";
    private static final String INT_ID_KEY = "pocket_id";
    private static final String ID_MAP_KEY = "ids";
    private static final String NEXT_ID_KEY = "next_id";

    public static final PocketDimensionIdManager INSTANCE = new PocketDimensionIdManager();

    public void remove(UUID uuid) {
        ids.remove(uuid);
    }

    private int nextId;
    //todo: should we store block position as well? would give freedom to change id hasher in the future
    private final Object2IntMap<UUID> ids = new Object2IntOpenHashMap<>();

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag entries = new ListTag();
        for (var entry : ids.object2IntEntrySet()) {
            CompoundTag tagEntry = new CompoundTag();
            tagEntry.putUUID(UUID_KEY, entry.getKey());
            tagEntry.putInt(INT_ID_KEY, entry.getIntValue());
            entries.add(tagEntry);
        }
        compoundTag.put(ID_MAP_KEY, entries);
        compoundTag.putInt(NEXT_ID_KEY, nextId);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ListTag entries = nbt.getList(ID_MAP_KEY, 10);
        int nextId = nbt.getInt(NEXT_ID_KEY);
        for (Tag tag : entries) {
            try {
                CompoundTag compoundTag = (CompoundTag) tag;
                UUID uuid = compoundTag.getUUID(UUID_KEY);
                int pocketId = compoundTag.getInt(INT_ID_KEY);
                ids.put(uuid, pocketId);
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("Failed to parse PocketDimensionIdManager entry {}: {}", tag, e.getMessage());
            }
        }
        this.nextId = nextId;
    }

    public int idFor(UUID uuid) {
        if (!ids.containsKey(uuid)) {
            ids.put(uuid, nextId);
            nextId++;
        }
        return ids.getInt(uuid);
    }

    public int idFor(Player player) {
        return idFor(player.getUUID());
    }

    public BlockPos originForId(int pocketDimensionId) {
        return BlockPos.containing(0, 2, 64 * pocketDimensionId);
    }

    public BlockPos originForPlayer(Player player) {
        return originForId(idFor(player));
    }

    public boolean maybeGeneratePocketRoom(ServerPlayer player) {
        var serverLevel = player.serverLevel();
        var pos = originForPlayer(player).below();
        var pocketLevel = serverLevel.getServer().getLevel(POCKET_DIMENSION);
        BlockState blockState = pocketLevel.getBlockState(pos);
        if (blockState.isAir()) {
            //todo: place structure nbt file
            BlockPos.MutableBlockPos fillPos = pos.mutable();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    pocketLevel.setBlock(fillPos, Blocks.OBSIDIAN.defaultBlockState(), 2);
                    fillPos.move(0, 0, 1);
                }
                fillPos.move(1, 0, -16);
            }
            return true;
        }
        return false;
    }
}
