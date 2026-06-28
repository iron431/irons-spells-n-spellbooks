package io.redspace.skillcasting.irons_spellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.void_tentacle.VoidTentacle;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class SculkTentaclesSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(30)
            .build();

    public SculkTentaclesSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 150;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        int rings = castContext.getOrDefault(SkillcastingComponentTypes.RING_COUNT, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(rings * 1.3f, 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.VOID_TENTACLES_START).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.VOID_TENTACLES_FINISH).toOpt();
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, 32, 0.15f, false);
        return true;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, baseSpellPower * getSpellPowerMultiplier(castContext));
        castContext.set(SkillcastingComponentTypes.RING_COUNT, 1 + castContext.getSkillLevel());
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        int rings = castContext.getOrDefault(SkillcastingComponentTypes.RING_COUNT, 0);
        int count = 2;
        Vec3 center = null;
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData != null && level instanceof ServerLevel serverLevel) {
            var target = targetData.getFirstEntityTarget(serverLevel);
            if (target != null) {
                center = target.position();
            }
        }
        if (center == null) {
            center = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 48f)
                    .checkForBlocks(true)
                    .bbInflation(0.15f)
                    .build()
                    .getLocation();
            center = Utils.moveToRelativeGroundLevel(level, center, 6);
        }

        LivingEntity owner = castContext.asEntityCaster() instanceof LivingEntity living ? living : null;
        if (owner instanceof Player player) {
            level.playSound(player, center.x, center.y, center.z, SoundRegistry.VOID_TENTACLES_FINISH.get(), SoundSource.AMBIENT, 1, 1);
        } else {
            level.playSound(null, center.x, center.y, center.z, SoundRegistry.VOID_TENTACLES_FINISH.get(), SoundSource.AMBIENT, 1, 1);
        }

        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        for (int r = 0; r < rings; r++) {
            float tentacles = count + r * 2;
            for (int i = 0; i < tentacles; i++) {
                Vec3 random = new Vec3(Utils.getRandomScaled(1), Utils.getRandomScaled(1), Utils.getRandomScaled(1));
                Vec3 spawn = center.add(new Vec3(0, 0, 1.3 * (r + 1)).yRot(((6.281f / tentacles) * i))).add(random);

                spawn = Utils.moveToRelativeGroundLevel(level, spawn, 8);
                if (!level.getBlockState(BlockPos.containing(spawn).below()).isAir() && owner != null) {
                    VoidTentacle tentacle = new VoidTentacle(level, owner, damage);
                    tentacle.moveTo(spawn);
                    tentacle.setYRot(Utils.random.nextInt(360));
                    level.addFreshEntity(tentacle);
                }
            }
        }
    }
}
