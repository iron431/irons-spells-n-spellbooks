package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @ModifyArg(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"),
            index = 4
    )
    private List<VoxelShape> mixin(List<VoxelShape> in) {
        List<VoxelShape> shieldCollisions = new ArrayList<>();
        level.getEntitiesOfClass(AbstractShieldEntity.class, bb.inflate(0.25)).stream().forEach((s) -> shieldCollisions.addAll(s.getVoxels()));
        return shieldCollisions;
    }
}
