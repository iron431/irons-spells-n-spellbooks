package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundGuidingBoltManagerStopTracking {

    private final UUID entity;

    public ClientboundGuidingBoltManagerStopTracking(Entity entity) {
        this.entity = entity.getUUID();
    }

    public ClientboundGuidingBoltManagerStopTracking(FriendlyByteBuf buf) {
        this.entity = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entity);

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuidingBoltManager.handleClientboundStopTracking(entity);
        });
        return true;
    }
}