package io.redspace.ironsspellbooks.spells.fire;


import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.ImpulseCastData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.damage.SpellSkillDamageSource;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcastingapi.core.CastType;
import io.redspace.skillcastingapi.data.ICastContext;
import io.redspace.skillcastingapi.data.ICastData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;


@AutoSpellConfig
public class BurningDashSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(ICastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getDamage(castContext)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public BurningDashSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
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
    public void onCast(ICastContext castContext) {
        if (!(castContext.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        float multiplier = (15 + getSpellPower(castContext)) / 12f;

        //Direction for Mobs to cast in
        Vec3 forward = castContext.getForward();
        //todo: reimplement burning dash cast data
//        if (playerMagicData.getAdditionalCastData() instanceof BurningDashDirectionOverrideCastData) {
//            if (Utils.random.nextBoolean())
//                forward = forward.yRot(90);
//            else
//                forward = forward.yRot(-90);
//        }

        //Create Dashing Movement Impulse
        var vec = forward.multiply(3, 1, 3).normalize().add(0, .25, 0).scale(multiplier);
        //Start Spin Attack
        if (entity.onGround()) {
            entity.setPos(entity.position().add(0, 1.5, 0));
            vec.add(0, 0.25, 0);
        }
        entity.setDeltaMovement(new Vec3(
                Mth.lerp(.75f, entity.getDeltaMovement().x, vec.x),
                Mth.lerp(.75f, entity.getDeltaMovement().y, vec.y),
                Mth.lerp(.75f, entity.getDeltaMovement().z, vec.z)
        ));
        entity.hurtMarked = true;


        entity.addEffect(new MobEffectInstance(MobEffectRegistry.BURNING_DASH, 15, getDamage(castContext), false, false, false));
        entity.invulnerableTime = 20;
        //todo: synced mob effects/other synced data
//        playerMagicData.getSyncedData().setSpinAttackType(SpinAttackType.FIRE);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFireTicks(80);
    }

    private int getDamage(ICastContext castContext) {
        return (int) (5 + getSpellPower(castContext));
    }

    public static void ambientParticles(ClientLevel level, LivingEntity entity) {
        //Vec3 motion = entity.getDeltaMovement().normalize().scale(-.25);
        for (int i = 0; i < 2; i++) {
            Vec3 random = Utils.getRandomVec3(.2);
            level.addParticle(ParticleHelper.FIRE, entity.getRandomX(0.75), entity.getY() + Utils.getRandomScaled(0.75), entity.getRandomZ(0.75), random.x, random.y, random.z);
        }
        for (int i = 0; i < 6; i++) {
            Vec3 random = Utils.getRandomVec3(.2);
            level.addParticle(ParticleHelper.EMBERS, entity.getRandomX(0.75), entity.getY() + Utils.getRandomScaled(0.75), entity.getRandomZ(0.75), random.x, random.y, random.z);
        }
    }

    //todo: replace with mutation to castcontext forward
    @Deprecated(forRemoval = true)
    public static class BurningDashDirectionOverrideCastData implements ICastData {
    }
}
