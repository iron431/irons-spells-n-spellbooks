package io.redspace.ironsspellbooks.network.particles;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class HealParticlesPacket implements CustomPacketPayload {

    private Vec3 pos;

    public HealParticlesPacket(Vec3 pos) {
        this.pos = pos;
    }

    public HealParticlesPacket(FriendlyByteBuf buf) {
        pos = readVec3(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        writeVec3(pos, buf);
    }

    public Vec3 readVec3(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new Vec3(x, y, z);
    }

    public void writeVec3(Vec3 vec3, FriendlyByteBuf buf) {
        buf.writeDouble(vec3.x);
        buf.writeDouble(vec3.y);
        buf.writeDouble(vec3.z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientSpellCastHelper.handleClientsideHealParticles(pos);
        });
        return true;
    }
}
