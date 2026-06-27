package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ExtendedEvokerFang;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;
import java.util.Optional;

public class FangStrikeSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.fang_count", castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(5)
            .build();

    public FangStrikeSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 30;
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
        return PlayableSound.standard(SoundEvents.EVOKER_PREPARE_ATTACK).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        // fixme: amplifier is clearly not the correct parameter, but need a better delineating between projectile count/summon count/spawn count/fang count
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 7 + castContext.getSkillLevel());
    }

    @Override
    public void onCast(Level world, CastContext castContext) {
        Vec3 forward = castContext.direction().multiply(1, 0, 1).normalize();
        Vec3 start = castContext.position().add(forward.scale(1.5));

        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        int count = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        Entity caster = castContext.asEntityCaster();
        float yRotDegrees = caster != null ? caster.getYRot() : castContext.getYRot() * Mth.RAD_TO_DEG;
        float fangYaw = (yRotDegrees - 90) * Mth.DEG_TO_RAD;
        for (int i = 0; i < count; i++) {
            Vec3 spawn = start.add(forward.scale(i));
            spawn = new Vec3(spawn.x, getGroundLevel(world, spawn, 8), spawn.z);
            if (!world.getBlockState(BlockPos.containing(spawn).below()).isAir()) {
                int delay = i / 3;
                ExtendedEvokerFang fang = new ExtendedEvokerFang(world, spawn.x, spawn.y, spawn.z, fangYaw, delay, castContext.asEntityCaster(), damage);
                world.addFreshEntity(fang);
            }
        }
    }

    private int getGroundLevel(Level level, Vec3 start, int maxSteps) {
        if (!level.getBlockState(BlockPos.containing(start)).isAir()) {
            for (int i = 0; i < maxSteps; i++) {
                start = start.add(0, 1, 0);
                if (level.getBlockState(BlockPos.containing(start)).isAir()) {
                    break;
                }
            }
        }
        Vec3 lower = level.clip(new ClipContext(start, start.add(0, maxSteps * -2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation();
        return (int) lower.y;
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast activeCast, Mob mob, LivingEntity target) {
        float f = activeCast.context().getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0) * 1.2f;
        return mob.distanceToSqr(target) > (f * f);
    }
}
