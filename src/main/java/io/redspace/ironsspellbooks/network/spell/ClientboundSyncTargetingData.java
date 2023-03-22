package io.redspace.ironsspellbooks.network.spell;

import io.redspace.ironsspellbooks.capabilities.magic.SpellTargetingData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncTargetingData {

    //private UUID targetUuid;
    private int targetId;

    public ClientboundSyncTargetingData(LivingEntity entity/*, UUID targetUuid*/) {
        //TODO: does this work for players?
        //this.targetUuid = targetUuid;
        this.targetId = entity.getId();
    }

    public ClientboundSyncTargetingData(FriendlyByteBuf buf) {
        //targetUuid = buf.readUUID();
        targetId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        //buf.writeUUID(targetUuid);
        buf.writeInt(targetId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().level.getEntity(targetId) instanceof LivingEntity livingEntity)
                ClientMagicData.setTargetingData(new SpellTargetingData(livingEntity));
        });
        return true;
    }
}