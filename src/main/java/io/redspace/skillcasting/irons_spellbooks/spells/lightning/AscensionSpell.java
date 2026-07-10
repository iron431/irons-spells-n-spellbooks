package io.redspace.skillcasting.irons_spellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AscensionSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public AscensionSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;
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
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 5f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        // addEffect requires LivingEntity
        Vec3 strikePos = Utils.moveToRelativeGroundLevel(level, castContext.position(PositionAnchor.CENTER),32);

        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        lightningBolt.setVisualOnly(true);
        lightningBolt.setDamage(0);
        lightningBolt.setPos(strikePos);
        level.addFreshEntity(lightningBolt);

        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 5f);
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float radiusSqr = radius * radius;
        Entity casterEntity = castContext.asEntityCaster();
        Vec3 casterPos = castContext.position();
        AABB searchBox = AABB.ofSize(casterPos, radius * 2, radius * 2, radius * 2);
        level.getEntities(casterEntity, searchBox).forEach(target -> {
            double distance = target.distanceToSqr(strikePos);
            if (distance < radiusSqr) {
                float finalDamage = (float) (damage * (1 - distance / radiusSqr));
                DamageSources.applyDamage(target, finalDamage, getDamageSource(level, lightningBolt, casterEntity));
                if (target instanceof Creeper creeper) {
                    creeper.thunderHit((ServerLevel) level, lightningBolt);
                }
                if (target instanceof LivingEntity livingEntity) {
                    livingEntity.knockback(
                            0.25f + finalDamage / 10f,
                            casterPos.x - livingEntity.getX(),
                            casterPos.z - livingEntity.getZ());
                }
            }
        });

        if (casterEntity != null) {
            Vec3 motion = castContext.direction().multiply(1, 0, 1).normalize().add(0, 5, 0).scale(.125);
            casterEntity.setDeltaMovement(casterEntity.getDeltaMovement().add(motion));
            casterEntity.hurtMarked = true;
            if (castContext.asEntityCaster() instanceof LivingEntity livingCaster) {
                livingCaster.addEffect(new MobEffectInstance(MobEffectRegistry.ASCENSION, 80, 0, false, false, true));
            }
        }
    }
}
