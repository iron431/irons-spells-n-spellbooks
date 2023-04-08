package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.capabilities.magic.SpellTargetingData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundSyncTargetingData {

    //private UUID targetUuid;
    private UUID targetUUID;
    private int targetId;

    public ClientboundSyncTargetingData(LivingEntity entity) {
        //For some reason client level doesnt have generic get by UUID. players need uuid, mobs need "id"
        targetUUID = entity.getUUID();
        if (entity instanceof Player) {
            targetId = -1;
        } else {
            targetId = entity.getId();
        }
    }

    public ClientboundSyncTargetingData(FriendlyByteBuf buf) {
        //targetUuid = buf.readUUID();
        targetUUID = buf.readUUID();
        targetId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(targetUUID);
        buf.writeInt(targetId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (targetId < 0) {
                ClientMagicData.setTargetingData(new SpellTargetingData(Minecraft.getInstance().level.getPlayerByUUID(targetUUID)));
            } else if (Minecraft.getInstance().level.getEntity(targetId) instanceof LivingEntity livingTarget)
                ClientMagicData.setTargetingData(new SpellTargetingData(livingTarget));

        });
        return true;
    }
}