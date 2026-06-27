package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.FireEruptionAoe;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RaiseHellSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(25)
            .setAllowCrafting(false)
            .build();

    public RaiseHellSpell() {
        this.manaCostPerLevel = 45;
        this.baseSpellPower = 15;
        this.spellPowerPerLevel = 0;
        this.castTime = 16;
        this.baseManaCost = 90;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float weaponDamage = castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        String plus = weaponDamage > 0 ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(damage + weaponDamage, 1) + plus),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.recast_count", castContext.find(SkillcastingComponentTypes.RECAST_CONFIG).map(RecastConfig::totalCasts).orElse(0))
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
    public boolean canBeInterrupted(@Nullable Player player) {
        return false;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.RAISE_HELL_PREPARE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.FIRE_ERUPTION_SLAM).toOpt();
    }

    @Override
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(castContext.getSkillLevel(), 80));
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_TIME, castTime);
        float weaponDamage = 0;
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            weaponDamage = Utils.getWeaponDamage(entity);
        }
        castContext.set(SkillcastingComponentTypes.WEAPON_DAMAGE, weaponDamage);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 8f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 8f);
        float range = 1.7f;
        Vec3 eyePos = castContext.asEntityCaster() instanceof LivingEntity living
                ? living.getEyePosition()
                : castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 forward = castContext.direction();
        Vec3 hitLocation = Utils.moveToRelativeGroundLevel(level,
                Utils.raycastForBlock(level, eyePos, eyePos.add(forward.multiply(range, 0, range)), ClipContext.Fluid.NONE).getLocation(), 4);
        FireEruptionAoe aoe = new FireEruptionAoe(level, radius);
        aoe.setOwner(castContext.asEntityCaster());
        aoe.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f) + castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f));
        aoe.setDamageSource(getDamageSource(level, aoe, castContext.asEntityCaster()).get());
        aoe.moveTo(hitLocation);
        level.addFreshEntity(aoe);
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 20 + (int) radius, hitLocation, radius * 2 + 5));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.OVERHEAD_MELEE_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of((casterRef, data, activeCast) -> {
            if (casterRef.get() instanceof LivingEntity living) {
                // fixme: ungate from entity
                ambientParticles(living);
            }
        });
    }

    public static void ambientParticles(LivingEntity entity) {
        Vec3 vec3 = entity.getBoundingBox().getCenter();
        for (int i = 0; i < 2; i++) {
            Vec3 pos = vec3.add(Utils.getRandomVec3(entity.getBbHeight() * 2));
            Vec3 motion = vec3.subtract(pos).scale(0.10f);
            entity.level().addParticle(ParticleHelper.EMBERS, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast cast, Mob mob, LivingEntity target) {
        float range = cast.context().getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 8f) * 1.1f;
        return Utils.raycastForBlock(mob.level(), mob.position(), mob.position().subtract(0, 0.5, 0), ClipContext.Fluid.NONE).getType() == HitResult.Type.MISS
                || target.distanceToSqr(mob) > range * range;
    }

    @Override
    public boolean allowLooting() {
        return false;
    }
}
