package com.example.testmod.entity.wall_of_fire;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.MagicManager;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.network.ServerboundCancelCast;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.fire.WallOfFireSpell;
import com.example.testmod.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class WallOfFireCastTracker extends Entity {
    public WallOfFireCastTracker(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.level = pLevel;
        if (ticksPerUpdate == 0)
            ticksPerUpdate = 1;
    }

    private LivingEntity caster;
    private Level level;
    private WallOfFireSpell.FireWallData fireWallData;
    private int ticksPerUpdate;
    private int tickCount;

    public WallOfFireCastTracker(Level level, LivingEntity castingEntity, WallOfFireSpell.FireWallData fireWallData, int ticksPerUpdate) {
        this(EntityRegistry.WALL_OF_FIRE_CAST_TRACKER.get(), level);
        this.caster = castingEntity;
        this.fireWallData = fireWallData;
        this.ticksPerUpdate = ticksPerUpdate;

    }

    @Override
    public void tick() {
        if (level.isClientSide)
            return;
        if (++tickCount % ticksPerUpdate == 0) {
            var vec = raycastForAnchor(level, caster);
            addAnchor(fireWallData, level, caster);
        }
    }

    private Vec3 raycastForAnchor(Level level, LivingEntity entity) {
        return Utils.getTargetBlock(level, entity, ClipContext.Fluid.ANY, 20).getLocation();
    }

    public void addAnchor(WallOfFireSpell.FireWallData fireWallData, Level level, LivingEntity entity) {
        Vec3 anchor = Utils.getTargetBlock(level, entity, ClipContext.Fluid.ANY, 20).getLocation();

        anchor = setOnGround(anchor, level);
        var anchorPoints = fireWallData.anchors;
        if (anchorPoints.size() == 0) {
            anchorPoints.add(anchor);

        } else {
            int i = anchorPoints.size();
            float distance = (float) anchorPoints.get(i - 1).distanceTo(anchor);
            float maxDistance = fireWallData.maxTotalDistance - fireWallData.accumulatedDistance;
            if (distance <= maxDistance) {
                //point fits, continue
                fireWallData.accumulatedDistance += distance;
                anchorPoints.add(anchor);
                //TestMod.LOGGER.debug("WallOfFire: this anchor fits (length {})", distance);

            } else {
                //too long, clip and cancel spell
                anchor = anchorPoints.get(i - 1).add(anchor.subtract(anchorPoints.get(i - 1)).normalize().scale(maxDistance));
                anchor = setOnGround(anchor, level);
                anchorPoints.add(anchor);
                if (entity instanceof ServerPlayer serverPlayer) {
                    var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
                    boolean triggerCooldown = playerMagicData.getCastSource() != CastSource.Scroll;
                    ServerboundCancelCast.cancelCast(serverPlayer, triggerCooldown);
                }
            }
            //TestMod.LOGGER.debug("WallOfFire.maxDistance: {}", this.maxTotalDistance);
            //TestMod.LOGGER.debug("WallOfFire.currentDistance: {}", this.accumulatedDistance);
        }
        MagicManager.spawnParticles(level, ParticleTypes.FLAME, anchor.x, anchor.y + 1.5, anchor.z, 5, .05, .25, .05, 0, true);
        TestMod.LOGGER.debug("WallOfFireSpell: adding anchor");
    }

    private Vec3 setOnGround(Vec3 in, Level level) {
        if (level.getBlockState(new BlockPos(in.x, in.y + .5f, in.z)).isAir()) {
            for (int i = 0; i < 15; i++) {
                if (!level.getBlockState(new BlockPos(in.x, in.y - i, in.z)).isAir()) {
                    return new Vec3(in.x, in.y - i + 1, in.z);
                }
            }
            return new Vec3(in.x, in.y - 15, in.z);
        } else {
            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) in.x, (int) in.z);
            return new Vec3(in.x, y, in.z);
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
