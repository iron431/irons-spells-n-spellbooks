package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class OnCastFinishedPacket implements CustomPacketPayload {
    private final String spellId;
    private final UUID castingEntityId;
    private final boolean cancelled;

    public OnCastFinishedPacket(UUID castingEntityId, String spellId, boolean cancelled) {
        this.spellId = spellId;
        this.castingEntityId = castingEntityId;
        this.cancelled = cancelled;
    }

    public OnCastFinishedPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        castingEntityId = buf.readUUID();
        cancelled = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeUUID(castingEntityId);
        buf.writeBoolean(cancelled);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientBoundOnCastFinished(castingEntityId, spellId, cancelled);
        });
        return true;
    }
}
