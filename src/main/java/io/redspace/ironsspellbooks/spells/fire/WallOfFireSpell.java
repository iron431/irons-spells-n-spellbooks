package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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

@AutoSpellConfig
public class WallOfFireSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "wall_of_fire");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getWallLength(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(30)
            .build();

    public WallOfFireSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new FireWallData(0);
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 3;
    }


    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
            var recast = playerMagicData.getPlayerRecasts().getRecastInstance(getSpellId());
            var fireWallData = (FireWallData) recast.getCastData();
            addAnchor(fireWallData, world, entity, recast);
        } else {
            var fireWallData = new FireWallData(getWallLength(spellLevel, entity));
            var recast = new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), 40, castSource, fireWallData);
            addAnchor(fireWallData, world, entity, recast);
            playerMagicData.getPlayerRecasts().addRecast(recast, playerMagicData);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onRecastFinished(ServerPlayer entity, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (!recastResult.isFailure()) {
            var level = entity.level;
            var fireWallData = (FireWallData) recastInstance.getCastData();
            if (fireWallData.anchorPoints.size() == 1) {
                addAnchor(fireWallData, level, entity, recastInstance);
            }

            if (fireWallData.anchorPoints.size() > 0) {
                WallOfFireEntity fireWall = new WallOfFireEntity(level, entity, fireWallData.anchorPoints, getDamage(recastInstance.getSpellLevel(), entity));
                Vec3 origin = fireWallData.anchorPoints.get(0);
                for (int i = 1; i < fireWallData.anchorPoints.size(); i++) {
                    origin.add(fireWallData.anchorPoints.get(i));
                }
                origin.scale(1 / (float) fireWallData.anchorPoints.size());
                fireWall.setPos(origin);
                level.addFreshEntity(fireWall);
            }
        }
        super.onRecastFinished(entity, recastInstance, recastResult, castDataSerializable);
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFireTime(4);
    }

    private float getWallLength(int spellLevel, LivingEntity entity) {
        return 10 + spellLevel * 3 * getEntityPowerMultiplier(entity);
    }

    private float getDamage(int spellLevel, LivingEntity sourceEntity) {
        return getSpellPower(spellLevel, sourceEntity);
    }

    public void addAnchor(FireWallData fireWallData, Level level, LivingEntity entity, RecastInstance recastInstance) {
        Vec3 anchor = Utils.getTargetBlock(level, entity, ClipContext.Fluid.ANY, 20).getLocation();

        anchor = setOnGround(anchor, level);
        var anchorPoints = fireWallData.anchorPoints;
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
            } else {
                //too long, clip and cancel spell
                anchor = anchorPoints.get(i - 1).add(anchor.subtract(anchorPoints.get(i - 1)).normalize().scale(maxDistance));
                anchor = setOnGround(anchor, level);
                anchorPoints.add(anchor);
                if (entity instanceof ServerPlayer serverPlayer) {
                    if (recastInstance.getRemainingRecasts() > 0) {
                        MagicData.getPlayerMagicData(serverPlayer).getPlayerRecasts().removeRecast(recastInstance, RecastResult.USED_ALL_RECASTS);
                    }
                }
            }
        }
        MagicManager.spawnParticles(level, ParticleTypes.FLAME, anchor.x, anchor.y + 1.5, anchor.z, 5, .05, .25, .05, 0, true);
    }

    private Vec3 setOnGround(Vec3 in, Level level) {
        if (level.getBlockState(BlockPos.containing(in.x, in.y + .5f, in.z)).isAir()) {
            for (int i = 0; i < 15; i++) {
                if (!level.getBlockState(BlockPos.containing(in.x, in.y - i, in.z)).isAir()) {
                    return new Vec3(in.x, in.y - i + 1, in.z);
                }
            }
            return new Vec3(in.x, in.y - 15, in.z);
        } else {
            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) in.x, (int) in.z);
            return new Vec3(in.x, y, in.z);
        }
    }

    public class FireWallData implements ICastDataSerializable {
        private Entity castingEntity;
        public List<Vec3> anchorPoints = new ArrayList<>();
        public float maxTotalDistance;
        public float accumulatedDistance;
        public int ticks;

        FireWallData(float maxTotalDistance) {
            this.maxTotalDistance = maxTotalDistance;
        }

        @Override
        public void reset() {

        }

        @Override
        public void writeToBuffer(FriendlyByteBuf buffer) {
            buffer.writeInt(anchorPoints.size());
            for (Vec3 vec : anchorPoints) {
                buffer.writeFloat((float) vec.x);
                buffer.writeFloat((float) vec.y);
                buffer.writeFloat((float) vec.z);
            }
        }

        @Override
        public void readFromBuffer(FriendlyByteBuf buffer) {
            anchorPoints = new ArrayList<>();
            int length = buffer.readInt();
            for (int i = 0; i < length; i++) {
                anchorPoints.add(new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag compoundTag = new CompoundTag();
            ListTag anchors = new ListTag();
            for (Vec3 vec : anchorPoints) {
                CompoundTag anchor = new CompoundTag();
                anchor.putFloat("x", (float) vec.x);
                anchor.putFloat("y", (float) vec.y);
                anchor.putFloat("z", (float) vec.z);
                anchors.add(anchor);
            }
            compoundTag.put("Anchors", anchors);
            return compoundTag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.anchorPoints = new ArrayList<>();
            if (nbt.contains("Anchors", 9)) {
                ListTag anchors = (ListTag) nbt.get("Anchors");
                for (Tag tag : anchors) {
                    if (tag instanceof CompoundTag anchor) {
                        this.anchorPoints.add(new Vec3(anchor.getDouble("x"), anchor.getDouble("y"), anchor.getDouble("z")));
                    }
                }
            }
        }
    }
}
