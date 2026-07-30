package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.data.IronsDataStorage;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.worldgen.ClearPortalFrameDataProcessor;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.ironsspellbooks.util.NBT;
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
import java.util.ArrayList;

public class PocketDimensionManager implements INBTSerializable<CompoundTag> {
    public static final ResourceKey<Level> POCKET_DIMENSION = ResourceKey.create(Registries.DIMENSION, IronsSpellbooks.id("pocket_dimension"));
    public static final ResourceLocation POCKET_ROOM_STRUCTURE = IronsSpellbooks.id("pocket_room");

    private static final String UUID_KEY = "uuid";
    private static final String INT_ID_KEY = "pocket_id";
    private static final String ID_MAP_KEY = "ids";
    private static final String RETURN_POS_KEY = "returns";

    public static final PocketDimensionManager INSTANCE = new PocketDimensionManager();

    public void remove(UUID uuid) {
        ids.remove(uuid);
        IronsDataStorage.INSTANCE.setDirty();
    }

    private final Object2IntMap<UUID> ids = new Object2IntOpenHashMap<>();
    private final ArrayList<PortalPos> returnPositions = new ArrayList<PortalPos>();

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag idEntries = new ListTag();
        for (var entry : ids.object2IntEntrySet()) {
            CompoundTag tagEntry = new CompoundTag();
            tagEntry.putUUID(UUID_KEY, entry.getKey());
            tagEntry.putInt(INT_ID_KEY, entry.getIntValue());
            idEntries.add(tagEntry);
        }
        compoundTag.put(ID_MAP_KEY, idEntries);
        ListTag returnEntries = new ListTag();
        for (var returnPos : returnPositions) {
            returnEntries.add(NBT.writePortalPos(returnPos));
        }
        compoundTag.put(RETURN_POS_KEY, returnEntries);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        int highestId = 0;
        ListTag idEntries = nbt.getList(ID_MAP_KEY, 10);
        for (Tag tag : idEntries) {
            try {
                CompoundTag compoundTag = (CompoundTag) tag;
                UUID uuid = compoundTag.getUUID(UUID_KEY);
                int pocketId = compoundTag.getInt(INT_ID_KEY);
                if (highestId < pocketId) { highestId = pocketId; }
                ids.put(uuid, pocketId);
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("Failed to parse PocketDimensionManager id entry: {}: {}", tag, e.getMessage());
            }
        }
        returnPositions.ensureCapacity(highestId+1);
        ListTag returnEntries = nbt.getList(RETURN_POS_KEY, 10);
        for (Tag tag : returnEntries) {
            try {
                CompoundTag compoundTag = (CompoundTag) tag;
                returnPositions.add(NBT.readPortalPos(compoundTag));
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("Failed to parse PocketDimensionManager position: {}: {}", tag, e.getMessage());
            }
        }
        // Place extra null values to make sure there are return slots for each id
        for (int i = returnPositions.size(); i <= highestId; i++) {
            returnPositions.add(null);
        }
    }

    public boolean hasId(UUID uuid) {
        return ids.containsKey(uuid);
    }

    public boolean hasId(Player player) {
        return hasId(player.getUUID());
    }

    private int idFor(UUID uuid) {
        if (!hasId(uuid)) {
            return -1;
        }
        return ids.getInt(uuid);
    }

    private int idFor(Player player) {
        return idFor(player.getUUID());
    }

    private BlockPos structurePosForId(int pocketDimensionId) {
        return BlockPos.containing(0, 0, ServerConfigs.POCKET_SPACING.get() * pocketDimensionId);
    }

    public BlockPos structurePosForPlayer(Player player) {
        return structurePosForId(idFor(player));
    }

    public PortalPos returnPosForPlayer(Player player) {
        return returnPositions.get(idFor(player));
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

    public boolean generatePocketIfUnassigned(ServerPlayer player) {
        UUID playerUUId = player.getUUID();
        if (!hasId(playerUUId))
        {
            // Assign new ID
            ids.put(playerUUId, returnPositions.size());
            returnPositions.add(PortalPos.of(player.level.dimension(), player.position(), player.getYRot()));
            var structurePos = structurePosForId(returnPositions.size());
            IronsDataStorage.INSTANCE.setDirty();
            // Create new pocket dimension
            var pocketLevel = player.serverLevel().getServer().getLevel(POCKET_DIMENSION);
            BlockState blockState = pocketLevel.getBlockState(structurePos);
            var structureTemplateManager = pocketLevel.getStructureManager();
            var structureTemplate = structureTemplateManager.getOrCreate(POCKET_ROOM_STRUCTURE);
            var placementSettings = (new StructurePlaceSettings()).setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true).addProcessor(new ClearPortalFrameDataProcessor());
            structureTemplate.placeInWorld(pocketLevel, structurePos, structurePos, placementSettings, pocketLevel.getRandom(), 2);
            return true;
        }
        return false;
    }

    public void updateReturn(ServerPlayer player) {
        returnPositions.set(idFor(player), PortalPos.of(player.level.dimension(), player.position(), player.getYRot()));
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
                    int pocketX = (int) (player.getX() / ServerConfigs.POCKET_SPACING.get()) * ServerConfigs.POCKET_SPACING.get();
                    int pocketZ = (int) (player.getZ() / ServerConfigs.POCKET_SPACING.get()) * ServerConfigs.POCKET_SPACING.get();
                    if (player.getX() < pocketX || player.getX() > pocketX + 16
                            || player.getZ() < pocketZ || player.getZ() > pocketZ + 16) {
                        // snap player back into bounds
                        var blockPos = structurePosForPlayer(player);
                        var portalPos = findPortalForStructure(serverLevel, blockPos);
                        player.resetFallDistance();
                        player.stopRiding();
                        player.moveTo(portalPos.getBottomCenter());
                    }
                }
            });
        }
    }
}
