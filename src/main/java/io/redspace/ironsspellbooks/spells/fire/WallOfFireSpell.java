package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.wall_of_fire.WallOfFireEntity;
import io.redspace.ironsspellbooks.network.ServerboundCancelCast;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class WallOfFireSpell extends AbstractSpell {
    public WallOfFireSpell() {
        this(1);
    }
    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getWallLength(), 1))
        );
    }
    public WallOfFireSpell(int level) {
        super(SpellType.WALL_OF_FIRE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 10;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getID() && playerMagicData.getAdditionalCastData() == null) {
            IronsSpellbooks.LOGGER.debug("WallOfFireSpell: creating new data");
            var fireWallData = new FireWallData(getWallLength());
            playerMagicData.setAdditionalCastData(fireWallData);

        }
        IronsSpellbooks.LOGGER.debug(playerMagicData.toString());
        //if (playerMagicData.getAdditionalCastData() instanceof FireWallData fireWallData)

        super.onCast(world, entity, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        IronsSpellbooks.LOGGER.debug("WallOfFireSpell.onServerCastTick");
        if (playerMagicData.getAdditionalCastData() instanceof FireWallData fireWallData) {
            IronsSpellbooks.LOGGER.debug("WallOfFireSpell.onServerCastTick {}", fireWallData.ticks);
            if (fireWallData.ticks++ % 4 == 0) {
                addAnchor(fireWallData, level, entity);
            }
        }

    }

    @Override
    public void onServerCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData, boolean isCancelled) {
//        for (Vec3 vec : spawnAnchors) {
//            irons_spellbooks.LOGGER.debug(vec.toString());
//
//        }
        super.onServerCastComplete(level, entity, playerMagicData, false);
        IronsSpellbooks.LOGGER.debug("WallOfFireSpell.onCastComplete");
        if (playerMagicData.getAdditionalCastData() instanceof FireWallData fireWallData) {
            if (fireWallData.anchors.size() == 1) {
                addAnchor(fireWallData, level, entity);
            }

            if (fireWallData.anchors.size() > 0) {
                WallOfFireEntity fireWall = new WallOfFireEntity(level, entity, fireWallData.anchors, getDamage(entity));
                Vec3 origin = fireWallData.anchors.get(0);
                for (int i = 1; i < fireWallData.anchors.size(); i++) {
                    origin.add(fireWallData.anchors.get(i));
                }
                origin.scale(1 / (float) fireWallData.anchors.size());
                fireWall.setPos(origin);
                level.addFreshEntity(fireWall);
            }
        }
    }

    private float getWallLength() {
        return 10 + level * 2;
    }

    private float getDamage(Entity sourceEntity) {
        return getSpellPower(sourceEntity);
    }

    public void addAnchor(FireWallData fireWallData, Level level, LivingEntity entity) {
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
                //irons_spellbooks.LOGGER.debug("WallOfFire: this anchor fits (length {})", distance);

            } else {
                //too long, clip and cancel spell
                anchor = anchorPoints.get(i - 1).add(anchor.subtract(anchorPoints.get(i - 1)).normalize().scale(maxDistance));
                anchor = setOnGround(anchor, level);
                anchorPoints.add(anchor);
                if (entity instanceof ServerPlayer serverPlayer) {
                    var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
                    boolean triggerCooldown = playerMagicData.getCastSource() != CastSource.SCROLL;
                    ServerboundCancelCast.cancelCast(serverPlayer, triggerCooldown);
                }
            }
            //irons_spellbooks.LOGGER.debug("WallOfFire.maxDistance: {}", this.maxTotalDistance);
            //irons_spellbooks.LOGGER.debug("WallOfFire.currentDistance: {}", this.accumulatedDistance);
        }
        MagicManager.spawnParticles(level, ParticleTypes.FLAME, anchor.x, anchor.y + 1.5, anchor.z, 5, .05, .25, .05, 0, true);
        IronsSpellbooks.LOGGER.debug("WallOfFireSpell: adding anchor");
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

    public class FireWallData implements CastData {
        private Entity castingEntity;
        public List<Vec3> anchors = new ArrayList<>();
        public float maxTotalDistance;
        public float accumulatedDistance;
        public int ticks;

        FireWallData(float maxTotalDistance) {
            this.maxTotalDistance = maxTotalDistance;
        }

        @Override
        public void reset() {

        }
    }
}
