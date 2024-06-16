package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundGuidingBoltManagerStartTracking {

    private final UUID entity;
    private final List<Integer> projectileIds;

    public ClientboundGuidingBoltManagerStartTracking(Entity entity, List<Projectile> projectiles) {
        this.entity = entity.getUUID();
        this.projectileIds = projectiles.stream().map(Entity::getId).toList();
    }

    public ClientboundGuidingBoltManagerStartTracking(FriendlyByteBuf buf) {
        projectileIds = new ArrayList<>();
        this.entity = buf.readUUID();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            projectileIds.add(buf.readInt());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entity);
        buf.writeInt(projectileIds.size());
        for (Integer projectileId : projectileIds) {
            buf.writeInt(projectileId);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuidingBoltManager.handleClientboundStartTracking(entity, projectileIds);
        });
        return true;
    }
}