package com.example.testmod.capabilities.mana.data;

import com.example.testmod.capabilities.mana.network.PacketSyncManaToClient;
import com.example.testmod.setup.Messages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.testmod.registries.AttributeRegistry.MAX_MANA;

public class ManaManager extends SavedData {

    private final Map<Integer, Mana> manaStorage = new HashMap<>();
    private final Random random = new Random();

    private int counter = 0;

    @Nonnull
    public static ManaManager get(Level level) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel) level).getDataStorage();
        return storage.computeIfAbsent(ManaManager::new, ManaManager::new, "manamanager");
    }

    @NotNull
    private Mana getManaInternal(int serverPlayerId) {
        return manaStorage.computeIfAbsent(serverPlayerId, cp -> new Mana(50));
    }

    public int getMana(ServerPlayer serverPlayer) {
        Mana mana = getManaInternal(serverPlayer.getId());
        return mana.getMana();
    }

    public int setMana(ServerPlayer serverPlayer, int newManaValue) {
        Mana mana = getManaInternal(serverPlayer.getId());
        mana.setMana(newManaValue);
        setDirty();
        return 1;
    }

    public void tick(Level level) {
        counter--;
        if (counter <= 0) {
            counter = 20;
            // Synchronize the mana to the players in this world
            // todo expansion: keep the previous data that was sent to the player and only send if changed
            level.players().forEach(player -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    var playerMana = serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA).map(PlayerMana::getRaw);

                    //int playerMana = serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA)
                    //        .map(PlayerMana::getMana)
                    //        .orElse(-1);
                    Mana mana = ManaManager.get(player.level).getManaInternal(serverPlayer.getId());
                    int playerMaxMana = (int)serverPlayer.getAttributeValue(MAX_MANA.get());
                    if(mana.getMana()<playerMaxMana)
                        mana.incrementMana((int)Math.max(playerMaxMana*.01f,1));
                    else
                        mana.setMana(playerMaxMana);
                    playerMana.get().setMana(mana.getMana());
                    Messages.sendToPlayer(new PacketSyncManaToClient(mana.getMana()), serverPlayer);
                }
            });
        }
    }

    public ManaManager() {
    }

    public ManaManager(CompoundTag tag) {
//        ListTag list = tag.getList("mana", Tag.TAG_COMPOUND);
//        for (Tag t : list) {
//            CompoundTag manaTag = (CompoundTag) t;
//            Mana mana = new Mana(manaTag.getInt("mana"));
//            ChunkPos pos = new ChunkPos(manaTag.getInt("x"), manaTag.getInt("z"));
//            manaMap.put(pos, mana);
//        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
//        ListTag list = new ListTag();
//        manaMap.forEach((chunkPos, mana) -> {
//            CompoundTag manaTag = new CompoundTag();
//            manaTag.putInt("x", chunkPos.x);
//            manaTag.putInt("z", chunkPos.z);
//            manaTag.putInt("mana", mana.getMana());
//            list.add(manaTag);
//        });
//        tag.put("mana", list);
//        return tag;
        return tag;
    }

}
