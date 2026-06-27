package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ChainCreeperSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public ChainCreeperSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 0;
        this.castTime = 30;
        this.baseManaCost = 40;
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
        return PlayableSound.standard(SoundEvents.CREEPER_PRIMED).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.EVOKER_CAST_SPELL).toOpt();
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        SkillcastingUtils.preCastTargetHelper(castContext, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 48f).intValue(), 0.25f, false);
        return true;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 48f);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        // fixme: amplifier is clearly not the correct parameter, but need a better delineating between projectile count/summon count/spawn count/fang count
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 3 + level - 1);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 spawn = null;
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData != null && level instanceof ServerLevel serverLevel) {
            var target = targetData.getFirstEntityTarget(serverLevel);
            if (target != null) {
                spawn = target.position();
            }
        }
        if (spawn == null) {
            HitResult raycast = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 32f)
                    .checkForBlocks(true)
                    .build();
            if (raycast.getType() == HitResult.Type.ENTITY) {
                spawn = ((EntityHitResult) raycast).getEntity().position();
            } else {
                spawn = Utils.moveToRelativeGroundLevel(level, raycast.getLocation().subtract(castContext.direction().normalize()).add(0, 2, 0), 5);
            }
        }

        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        int count = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        summonCreeperRing(level, castContext.asEntityCaster(), spawn.add(0, 0.5, 0), damage, count);
    }

    public static void summonCreeperRing(Level level, @Nullable Entity owner, Vec3 origin, float damage, int count) {
        if (count < 3) {
            count = 3;
        }
        int degreesPerCreeper = 360 / count;
        for (int i = 0; i < count; i++) {
            Vec3 motion = new Vec3(0, 0, .4 + count * .015f);
            motion = motion.xRot(75 * Mth.DEG_TO_RAD);
            motion = motion.yRot(degreesPerCreeper * i * Mth.DEG_TO_RAD);

            CreeperHeadProjectile head = new CreeperHeadProjectile(owner, level, motion, damage);
            head.setChainOnKill(true);
            head.setChainCount(count - 2);
            Vec3 spawn = origin.add(motion.multiply(1, 0, 1).normalize().scale(.6f));
            var angle = Utils.rotationFromDirection(motion);

            head.moveTo(spawn.x, spawn.y - head.getBoundingBox().getYsize() / 2, spawn.z, angle.y, angle.x);
            level.addFreshEntity(head);
        }
    }
}
