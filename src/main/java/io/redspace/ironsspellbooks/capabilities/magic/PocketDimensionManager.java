package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.worldgen.ClearPortalFrameDataProcessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public class PocketDimensionManager implements INBTSerializable<CompoundTag> {
    public static final ResourceKey<Level> POCKET_DIMENSION = ResourceKey.create(Registries.DIMENSION, IronsSpellbooks.id("pocket_dimension"));
    public static final ResourceLocation POCKET_ROOM_STRUCTURE = IronsSpellbooks.id("pocket_room");
    public static final int POCKET_SPACING = 256;

    private static final String UUID_KEY = "uuid";
    private static final String INT_ID_KEY = "pocket_id";
    private static final String ID_MAP_KEY = "ids";
    private static final String NEXT_ID_KEY = "next_id";

    public static final PocketDimensionManager INSTANCE = new PocketDimensionManager();

    public void remove(UUID uuid) {
        ids.remove(uuid);
        IronsDataStorage.INSTANCE.setDirty();
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
                IronsSpellbooks.LOGGER.error("Failed to parse PocketDimensionManager id entry: {}: {}", tag, e.getMessage());
            }
        }
        this.nextId = nextId;
    }

    //todo: should this be the trigger for generating a new platform? getOrCreateRoomId? i think so
    public int idFor(UUID uuid) {
        if (!ids.containsKey(uuid)) {
            ids.put(uuid, nextId);
            nextId++;
            IronsDataStorage.INSTANCE.setDirty();
        }
        return ids.getInt(uuid);
    }

    public int idFor(Player player) {
        return idFor(player.getUUID());
    }

    public BlockPos structurePosForId(int pocketDimensionId) {
        return BlockPos.containing(0, 0, POCKET_SPACING * pocketDimensionId);
    }

    public BlockPos structurePosForPlayer(Player player) {
        return structurePosForId(idFor(player));
    }

    public BlockPos findPortalForStructure(ServerLevel pocketDimension, BlockPos blockPos) {
        BlockPos defaultPos = blockPos.south(10).east(7).above(2);
        if (pocketDimension.getBlockState(defaultPos).is(BlockRegistry.POCKET_PORTAL_FRAME)) {
            return defaultPos;
        } else {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 32; y++) {
                        BlockPos pos = blockPos.south(x).east(z).above(y);
                        if (pocketDimension.getBlockState(pos).is(BlockRegistry.POCKET_PORTAL_FRAME)) {
                            return pos;
                        }
                    }
                }
            }
        }
        return defaultPos;
    }

    public boolean maybeGeneratePocketRoom(ServerPlayer player) {
        var serverLevel = player.serverLevel();
        var structurePos = structurePosForPlayer(player);
        var pocketLevel = serverLevel.getServer().getLevel(POCKET_DIMENSION);
        BlockState blockState = pocketLevel.getBlockState(structurePos);
        if (blockState.isAir() && !blockState.is(Blocks.BARRIER)) {
            var structureTemplateManager = pocketLevel.getStructureManager();
            var structureTemplate = structureTemplateManager.getOrCreate(POCKET_ROOM_STRUCTURE);
            var placementSettings = (new StructurePlaceSettings()).setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true).addProcessor(new ClearPortalFrameDataProcessor());
            structureTemplate.placeInWorld(pocketLevel, structurePos, structurePos, placementSettings, pocketLevel.getRandom(), 2);
            return true;
        }
        return false;
    }

    public void tick(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
            return;
        }
        if (serverLevel.getGameTime() % 100 == 0) {
            serverLevel.players().forEach(player -> {
                if (!player.isCreative() && !player.isSpectator()) {
                    int pocketX = (int) (player.getX() / PocketDimensionManager.POCKET_SPACING) * PocketDimensionManager.POCKET_SPACING;
                    int pocketZ = (int) (player.getZ() / PocketDimensionManager.POCKET_SPACING) * PocketDimensionManager.POCKET_SPACING;
                    if (player.getX() < pocketX || player.getX() > pocketX + 16
                            || player.getZ() < pocketZ || player.getZ() > pocketZ + 16) {
                        // snap player back into bounds
                        var blockPos = structurePosForPlayer(player);
                        var portalPos = findPortalForStructure(serverLevel, blockPos);
                        player.resetFallDistance();
                        player.moveTo(portalPos.getBottomCenter());
                    }
                }
            });
        }
    }
}
