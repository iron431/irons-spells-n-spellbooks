package io.redspace.skillcasting.irons_spellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class LightningBoltSpell extends AbstractSpellSkill {

    private static final float STRIKE_RANGE = 64f;
    private static final float IMPACT_RADIUS = 4f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(25)
            .build();

    public LightningBoltSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 75;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)));
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, STRIKE_RANGE);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, IMPACT_RADIUS);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, STRIKE_RANGE);
        var result = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, range)
                .checkForBlocks(true)
                .bbInflation(1f)
                .build();
        Vec3 pos = result.getLocation();
        if (result.getType() == HitResult.Type.ENTITY) {
            pos = ((EntityHitResult) result).getEntity().position();
        } else {
            pos = Utils.moveToRelativeGroundLevel(level, pos, 10);
        }

        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        lightningBolt.setVisualOnly(true);
        lightningBolt.setDamage(0);
        lightningBolt.setPos(pos);
        level.addFreshEntity(lightningBolt);

        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, IMPACT_RADIUS);
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        Entity caster = castContext.asEntityCaster();
        Vec3 strikePos = pos;
        float radiusSqr = radius * radius;
        level.getEntities(caster, AABB.ofSize(strikePos, radius * 2, radius * 2, radius * 2), target -> canHit(caster, target))
                .forEach(target -> {
                    double distance = target.distanceToSqr(strikePos);
                    if (distance < radiusSqr && Utils.hasLineOfSight(level, strikePos.add(0, 2, 0), target.getBoundingBox().getCenter(), true)) {
                        float finalDamage = (float) (damage * (1 - distance / radiusSqr));
                        DamageSources.applyDamage(target, finalDamage, getDamageSource(level, lightningBolt, caster));
                        if (target instanceof Creeper creeper) {
                            creeper.thunderHit((ServerLevel) level, lightningBolt);
                        }
                    }
                });
    }

    private static boolean canHit(Entity owner, Entity target) {
        return target != owner && target.isAlive() && target.isPickable() && !target.isSpectator();
    }
}
