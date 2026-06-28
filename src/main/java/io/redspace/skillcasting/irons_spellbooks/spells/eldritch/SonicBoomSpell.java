package io.redspace.skillcasting.irons_spellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class SonicBoomSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(25)
            .build();

    public SonicBoomSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 8;
        this.castTime = 30;
        this.baseManaCost = 110;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1))
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
        return PlayableSound.standard(SoundEvents.WARDEN_SONIC_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.of(SoundRegistry.SONIC_BOOM, 3.5f, 0.9f, 1.1f).toOpt();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_SPIT_ANIMATION;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 15f + 5f * level);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, castContext.position(PositionAnchor.ORIGIN), 20, 10f));

        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 15f);
        Vec3 start = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 forward = castContext.direction();
        Vec3 end = start.add(forward.scale(range));
        AABB boundingBox = caster != null ? caster.getBoundingBox().expandTowards(end.subtract(start)) : new AABB(start, end);

        for (Entity target : level.getEntities(caster, boundingBox)) {
            HitResult hit = Utils.checkEntityIntersecting(target, start, end, 0.4f);
            if (hit.getType() != HitResult.Type.MISS) {
                DamageSources.applyDamage(target, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), getDamageSource(level, caster));
            }
        }

        Vec3 look = forward.normalize();
        for (int i = 0; i < range; i++) {
            Vec3 particlePos = look.scale(i).add(start);
            MagicManager.spawnParticles(level, ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0, false);
        }
    }
}
