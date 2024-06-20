package io.redspace.ironsspellbooks.network.spell;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractVec3Packet {

    Vec3 pos;

    public AbstractVec3Packet(Vec3 pos) {
        this.pos = pos;
    }

    public AbstractVec3Packet(FriendlyByteBuf buf) {
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

    public abstract boolean handle(Supplier<NetworkEvent.Context> supplier);
}
