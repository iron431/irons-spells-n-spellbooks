package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.AnimatedAttacker;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncAnimation<T extends Entity & AnimatedAttacker> {
    int entityId;
    int animationId;

    public ClientboundSyncAnimation(int animationId, T entity) {
        this.entityId = entity.getId();
        this.animationId = animationId;
    }

    public ClientboundSyncAnimation(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        animationId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(animationId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            var entity = level.getEntity(entityId);
            if (entity instanceof AnimatedAttacker animatedAttacker) {
                animatedAttacker.playAnimation(animationId);
            }
        });

        return true;
    }
}
