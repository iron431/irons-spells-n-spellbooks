package io.redspace.skillcasting.irons_spellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VoltStrikeSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public VoltStrikeSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f).intValue()));
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
        castContext.set(SkillcastingComponentTypes.DAMAGE, 5 + getSpellPower(castContext));
    }

    @Override
    public void onClientCastComplete(CastContext castContext, CastEndReason castEndReason) {
        super.onClientCastComplete(castContext, castEndReason);
        MagicData.get(castContext.caster().get()).setSpinAttackType(SpinAttackType.LIGHTNING);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() == null) {
            return;
        }
        Entity entity = castContext.asEntityCaster();
        entity.hasImpulse = true;
        float multiplier = (15 + getSpellPower(castContext)) / 20f;

        Vec3 forward = castContext.direction();
        float upwardness = (float) forward.dot(new Vec3(0, 1, 0));
        float remap = 1 - (Math.max(0, upwardness) * 0.6f);
        Vec3 impulse = forward.scale(3 * multiplier).multiply(1, remap, 1);
        if (entity.onGround()) {
            entity.setPos(entity.position().add(0, 1.5, 0));
            impulse = impulse.add(0, 0.5, 0);
        } else {
            impulse = impulse.add(0, 0.25, 0);
        }
        entity.setDeltaMovement(new Vec3(
                Mth.lerp(.75f, entity.getDeltaMovement().x, impulse.x),
                Mth.lerp(.75f, entity.getDeltaMovement().y, impulse.y),
                Mth.lerp(.75f, entity.getDeltaMovement().z, impulse.z)
        ));
        entity.hurtMarked = true;
        entity.invulnerableTime = 20;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(
                    MobEffectRegistry.VOLT_STRIKE,
                    10,
                    castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f).intValue(),
                    false, false, false));
        }
        MagicData.get(entity).setSpinAttackType(SpinAttackType.LIGHTNING);
    }
}
