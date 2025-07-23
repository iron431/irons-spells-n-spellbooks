package io.redspace.ironsspellbooks.spells.fire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.SpellSkillDamageSource;
import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import io.redspace.skillcastingapi.core.AutoCastDataSerializer;
import io.redspace.skillcastingapi.data.CastDataSerializer;
import io.redspace.skillcastingapi.data.ICastContext;
import io.redspace.skillcastingapi.data.RecastInstance;
import io.redspace.skillcastingapi.data.SkillcastingData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class WallOfFireSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(ICastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(getDamage(castContext), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getWallLength(castContext), 1))
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
    public io.redspace.skillcastingapi.core.CastType getCastType() {
        return io.redspace.skillcastingapi.core.CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

//    @Override
//    public io.redspace.skillcastingapi.data.ICastDataSerializable<?> getEmptyCastData() {
//        return new FireWallData(0);
//    }
//
//    @Override
//    public int getRecastCount(ICastContext castContext) {
//        return 3;
//    }

    @Override
    public Optional<RecastInstance.Configuration> getDefaultRecastConfiguration(ICastContext castContext) {
        return Optional.of(new RecastInstance.Configuration(3, 20 * 3));
    }

    @Override
    public void onCast(ICastContext castContext) {
        var skillcastingData = castContext.getSkillcastingData();
        var recastInstance = skillcastingData.getRecasts().getRecastInstance(this);
        if (recastInstance == null) {
            return;
        }
        if (recastInstance.isFirstRecast()) {
            recastInstance.setCastData(new FireWallData(getWallLength(castContext)));
        }
        var fireWallData = (FireWallData) recastInstance.getCastData();
        addAnchor(fireWallData, castContext);
    }

    @Override
    public void onRecastFinished(ICastContext castContext, io.redspace.skillcastingapi.data.RecastInstance recastInstance, io.redspace.skillcastingapi.data.RecastResult recastResult, io.redspace.skillcastingapi.data.ICastDataSerializable<?> castData) {
        if (!recastResult.isFailure()) {
            var level = castContext.getLevel();
            var fireWallData = (FireWallData) recastInstance.getCastData();
            if (fireWallData.anchorPoints.size() == 1) {
                addAnchor(fireWallData, castContext);
            }

            if (fireWallData.anchorPoints.size() > 0) {
                WallOfFireEntity fireWall = new WallOfFireEntity(level, castContext.getEntity(), fireWallData.anchorPoints, getDamage(castContext));
                Vec3 origin = fireWallData.anchorPoints.get(0);
                for (int i = 1; i < fireWallData.anchorPoints.size(); i++) {
                    origin.add(fireWallData.anchorPoints.get(i));
                }
                origin.scale(1 / (float) fireWallData.anchorPoints.size());
                fireWall.setPos(origin);
                level.addFreshEntity(fireWall);
            }
        }
        super.onRecastFinished(castContext, recastInstance, recastResult, castData);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFireTicks(80);
    }

    private float getWallLength(ICastContext castContext) {
        return 10 + castContext.getSpellLevel() * 3 * getEntityPowerMultiplier(Utils.getLivingEntity(castContext));
    }

    private float getDamage(ICastContext castContext) {
        return getSpellPower(castContext);
    }

    public void addAnchor(FireWallData fireWallData, ICastContext castContext) {
        var level = castContext.getLevel();
        Vec3 anchor = Utils.getTargetBlock(level, castContext.getPosition(), castContext.getForward(), 20, ClipContext.Fluid.ANY).getLocation();

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
                if (castContext.getEntity() instanceof ServerPlayer serverPlayer) {
                    SkillcastingData.get(serverPlayer).getRecasts().cancelRecast(serverPlayer, this, io.redspace.skillcastingapi.data.RecastResult.USED_ALL_RECASTS);
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

    public static class FireWallData implements io.redspace.skillcastingapi.data.ICastDataSerializable<FireWallData> {
        public List<Vec3> anchorPoints = new ArrayList<>();
        public float maxTotalDistance;
        public float accumulatedDistance;
        public int ticks;


        FireWallData(float maxTotalDistance) {
            this.maxTotalDistance = maxTotalDistance;
        }

        FireWallData(List<Vector3f> anchors) {
            this.anchorPoints = anchors.stream().map(Utils::v3d).toList();
        }

        @AutoCastDataSerializer
        static class Serializer extends CastDataSerializer<FireWallData> {
            static Serializer INSTANCE = new Serializer();
            private static final StreamCodec<RegistryFriendlyByteBuf, FireWallData> STREAM_CODEC = StreamCodec.of(
                    (buffer, data) -> {
                        buffer.writeInt(data.anchorPoints.size());
                        for (Vec3 vec : data.anchorPoints) {
                            buffer.writeFloat((float) vec.x);
                            buffer.writeFloat((float) vec.y);
                            buffer.writeFloat((float) vec.z);
                        }
                    },
                    buffer -> {
                        var anchorPoints = new ArrayList<Vector3f>();
                        int length = buffer.readInt();
                        for (int i = 0; i < length; i++) {
                            anchorPoints.add(new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));
                        }
                        return new FireWallData(anchorPoints);
                    }
            );

            private static final Codec<FireWallData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.list(ExtraCodecs.VECTOR3F).fieldOf("anchors").forGetter(data -> data.anchorPoints.stream().map(Utils::v3f).toList())
            ).apply(builder, FireWallData::new));

            @Override
            public ResourceLocation getId() {
                return IronsSpellbooks.id("wof_cast_data_serializer");
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, FireWallData> streamCodec() {
                return STREAM_CODEC;
            }

            @Override
            public Codec<FireWallData> codec() {
                return CODEC;
            }
        }

        @Override
        public CastDataSerializer<FireWallData> getSerializer() {
            return Serializer.INSTANCE;
        }
    }
}
