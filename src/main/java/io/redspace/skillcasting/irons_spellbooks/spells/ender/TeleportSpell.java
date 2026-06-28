package io.redspace.skillcasting.irons_spellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.particles.TeleportParticlesPacket;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;
import java.util.Optional;

public class TeleportSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(3)
            .build();

    public TeleportSpell() {
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 10;
        this.baseManaCost = 20;
        this.manaCostPerLevel = 5;
        this.castTime = 0;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1)));
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.of(SoundEvents.ENDERMAN_TELEPORT, 2f, 1f, 1f).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, getDistance(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(castContext.asEntityCaster() instanceof LivingEntity entity)) {
            return;
        }
        Vec3 dest = castContext.getOrNull(SkillcastingComponentTypes.TARGET_POSITION);
        if (dest == null) {
            dest = findTeleportLocation(level, entity, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f));
        }
        final Vec3 teleportDest = dest;

        castContext.caster().distributeToClients(new TeleportParticlesPacket(entity.position(), teleportDest));

        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        Utils.handleSpellTeleport(null, entity, teleportDest);
        entity.resetFallDistance();

        getOnCastSound(castContext).ifPresent(sound -> level.playSound(null, teleportDest.x, teleportDest.y, teleportDest.z,
                sound.soundEventHolder().value(), SoundSource.NEUTRAL, sound.volume(), sound.samplePitch(level.getRandom())));
    }

    public static Vec3 findTeleportLocation(Level level, LivingEntity entity, float maxDistance) {
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, maxDistance);
        return solveTeleportDestination(level, entity, blockHitResult.getBlockPos(), blockHitResult.getLocation());
    }

    public static Vec3 solveTeleportDestination(Level level, LivingEntity entity, BlockPos blockPos, Vec3 vec3) {
        BlockPos pos = blockPos;
        Vec3 bbOffset = entity.getForward().normalize().multiply(entity.getBbWidth() / 3, 0, entity.getBbHeight() / 3);
        Vec3 bbImpact = vec3.subtract(bbOffset);

        double ledgeY = level.clip(new ClipContext(Vec3.atBottomCenterOf(pos).add(0, 3, 0), Vec3.atBottomCenterOf(pos),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation().y;
        boolean isAir = level.getBlockState(new BlockPos(new Vec3i(pos.getX(), (int) ledgeY, pos.getZ())).above()).isAir();
        boolean los = level.clip(new ClipContext(bbImpact, bbImpact.add(0, ledgeY - pos.getY(), 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;

        if (isAir && los && Math.abs(ledgeY - pos.getY()) <= 3) {
            return new Vec3(pos.getX() + 0.5, ledgeY + 0.001, pos.getZ() + 0.5);
        }
        return level.clip(new ClipContext(bbImpact, bbImpact.add(0, -entity.getBbHeight(), 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getLocation().add(0, 0.001, 0);
    }

    public static void particleCloud(Level level, Vec3 pos) {
        if (level.isClientSide) {
            double width = 0.5;
            float height = 1;
            for (int i = 0; i < 55; i++) {
                double x = pos.x + Utils.random.nextDouble() * width * 2 - width;
                double y = pos.y + height + Utils.random.nextDouble() * height * 1.2 * 2 - height * 1.2;
                double z = pos.z + Utils.random.nextDouble() * width * 2 - width;
                double dx = Utils.random.nextDouble() * 0.1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dy = Utils.random.nextDouble() * 0.1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dz = Utils.random.nextDouble() * 0.1 * (Utils.random.nextBoolean() ? 1 : -1);
                level.addParticle(ParticleTypes.PORTAL, true, x, y, z, dx, dy, dz);
            }
        }
    }

    private float getDistance(CastContext castContext) {
        return (float) (Utils.softCapFormula(getSpellPowerMultiplier(castContext)) * getSpellPower(castContext));
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }
}
