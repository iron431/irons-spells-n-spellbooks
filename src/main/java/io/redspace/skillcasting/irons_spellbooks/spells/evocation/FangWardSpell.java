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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class FangWardSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.ring_count", castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(15)
            .build();

    public FangWardSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 45;
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
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 2 + (level - 1) / 3);
    }

    @Override
    public void onCast(Level world, CastContext castContext) {
        int rings = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        int count = 5;
        Vec3 center = castContext.position();
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float yRot = castContext.getYRot();

        for (int r = 0; r < rings; r++) {
            float fangs = count + r * r;
            for (int i = 0; i < fangs; i++) {
                Vec3 spawn = center.add(new Vec3(0, 0, 1.5 * (r + 1)).yRot(yRot * Mth.DEG_TO_RAD + ((6.281f / fangs) * i)));
                spawn = Utils.moveToRelativeGroundLevel(world, spawn, 5);
                if (!world.getBlockState(BlockPos.containing(spawn).below()).isAir()) {
                    ExtendedEvokerFang fang = new ExtendedEvokerFang(world, spawn.x, spawn.y, spawn.z, get2DAngle(center, spawn), r, castContext.asEntityCaster(), damage);
                    world.addFreshEntity(fang);
                }
            }
        }
    }

    private float get2DAngle(Vec3 a, Vec3 b) {
        return Utils.getAngle(new Vec2((float) a.x, (float) a.z), new Vec2((float) b.x, (float) b.z));
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast activeCast, Mob mob, LivingEntity target) {
        float d = 1.5f * (activeCast.context().getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0) + 1);
        return mob.distanceToSqr(target) > d * d * 1.2f;
    }
}
