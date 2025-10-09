package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.api.backwards_compat.UpgradeTypeCache;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class SyncUpgradeOrbTypes implements CustomPacketPayload {
    public final List<ResourceKey<UpgradeOrbType>> types;

    public SyncUpgradeOrbTypes(Collection<ResourceKey<UpgradeOrbType>> types) {
        this.types = new ArrayList<>();
        this.types.addAll(types);
    }

    public SyncUpgradeOrbTypes(FriendlyByteBuf pBuffer) {
        int i = pBuffer.readInt();
        this.types = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            types.add(pBuffer.readResourceKey(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY));
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void toBytes(FriendlyByteBuf pBuffer) {
        int i = types.size();
        pBuffer.writeInt(i);
        for (int j = 0; j < i; j++) {
            pBuffer.writeResourceKey(types.get(j));
        }
    }


    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            try {
                UpgradeTypeCache.onClientLoad(Minecraft.getInstance().player.level.registryAccess(), this.types);
            } catch (Exception e) {
                IronsSpellbooks.LOGGER.error("Failed to sync upgrade orb types: {}", e.getMessage());
            }
        });

        return true;
    }
}
