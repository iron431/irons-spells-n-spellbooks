package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.particle.ClientShieldHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(Particle.class)
public class ParticleMixin {
    @Shadow
    protected ClientLevel level;
    @Shadow
    private AABB bb;

    private List<VoxelShape> old(List<VoxelShape> in) {
        List<VoxelShape> shieldCollisions = new ArrayList<>();
        level.getEntitiesOfClass(AbstractShieldEntity.class, bb.inflate(0.25)).stream().forEach((s) -> shieldCollisions.addAll(s.getVoxels()));
        return shieldCollisions;
    }

    private List<VoxelShape> newer(List<VoxelShape> in) {
        return ClientShieldHelper.getShieldsFor(bb.inflate(0.25));
    }

    @ModifyArg(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"),
            index = 4
    )
    private List<VoxelShape> mixin(List<VoxelShape> in) {
//        return ClientShieldHelper.getShieldsFor(bb.inflate(0.25)).flatMap(shield -> shield.getVoxels().stream()).toList();
        switch (phase) {
            case 0: {
                IronsSpellbooks.LOGGER.debug("init...");
                tracker = System.currentTimeMillis();
                phase++;
                break;
            }
            case 1: {
                if (System.currentTimeMillis() > tracker + TEN_SECONDS) {
                    IronsSpellbooks.LOGGER.debug("starting particle profile");
                    tracker = System.currentTimeMillis();
                    phase++;
                    opcount = 0;
                }
                break;
            }
            case 2: {
                if (System.currentTimeMillis() > tracker + TEN_SECONDS) {
                    IronsSpellbooks.LOGGER.debug("Old opcode result: {} ({} ms per op)", opcount, 10_000.0 / opcount);
                    IronsSpellbooks.LOGGER.debug("switching modes");
                    tracker = System.currentTimeMillis();
                    opcount = 0;
                    phase++;
                }
                break;
            }
            case 3: {
                if (System.currentTimeMillis() > tracker + TEN_SECONDS) {
                    IronsSpellbooks.LOGGER.debug("New opcode result: {} ({} ms per op)", opcount, 10_000.0 / opcount);
                    IronsSpellbooks.LOGGER.debug("done profile");
                    phase++;
                }
                break;
            }
        }
        List<VoxelShape> result;
        int i = 0;
        do {
            opcount++;
            if (phase >= 3) {
                result = newer(in);
            } else {
                result = old(in);
            }
        } while (i++ < 200);
        return result;
    }

    private static int opcount = 0;
    private static Long TEN_SECONDS = 10_000L;
    @Unique
    private static Long tracker = -1L;
    private static int phase = 0;
}
