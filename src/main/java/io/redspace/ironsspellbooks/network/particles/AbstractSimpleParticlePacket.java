package io.redspace.ironsspellbooks.network.ported.particles;

import io.redspace.ironsspellbooks.network.ported.AbstractVec3Packet;
import net.minecraft.world.phys.Vec3;


import java.util.function.Consumer;

public abstract class AbstractSimpleParticlePacket extends AbstractVec3Packet {
    public AbstractSimpleParticlePacket(Vec3 pos) {
        super(pos);
    }

//    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
//        NetworkEvent.Context ctx = supplier.get();
//        ctx.enqueueWork(() -> {
//            particleFunction().accept(pos);
//        });
//        return true;
//    }

    abstract Consumer<Vec3> particleFunction();
}
