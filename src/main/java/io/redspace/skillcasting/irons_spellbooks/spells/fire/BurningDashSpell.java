package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class BurningDashSpell extends AbstractSpellSkill {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "burning_dash");

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f)));
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
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 5 + getSpellPower(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (castContext.asEntityCaster() == null) {
            return;
        }
        Entity entity = castContext.asEntityCaster();
        entity.hurtMarked = true;
        float multiplier = (15 + getSpellPower(castContext)) / 12f;

        Vec3 forward = castContext.direction();
        var vec = forward.multiply(3, 1, 3).normalize().add(0, .25, 0).scale(multiplier);
        if (entity.onGround()) {
            entity.setPos(entity.position().add(0, 1.5, 0));
            vec.add(0, 0.25, 0);
        }
        entity.setDeltaMovement(new Vec3(
                Mth.lerp(.75f, entity.getDeltaMovement().x, vec.x),
                Mth.lerp(.75f, entity.getDeltaMovement().y, vec.y),
                Mth.lerp(.75f, entity.getDeltaMovement().z, vec.z)
        ));
        entity.invulnerableTime = 20;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.BURNING_DASH, 15, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f).intValue(), false, false, false));
        }
        MagicData.get(entity).setSpinAttackType(SpinAttackType.FIRE);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setFireTicks(80);
    }

    public static void ambientParticles(Level level, LivingEntity entity) {
        for (int i = 0; i < 2; i++) {
            Vec3 random = Utils.getRandomVec3(.2);
            level.addParticle(ParticleHelper.FIRE, entity.getRandomX(0.75), entity.getY() + Utils.getRandomScaled(0.75), entity.getRandomZ(0.75), random.x, random.y, random.z);
        }
        for (int i = 0; i < 6; i++) {
            Vec3 random = Utils.getRandomVec3(.2);
            level.addParticle(ParticleHelper.EMBERS, entity.getRandomX(0.75), entity.getY() + Utils.getRandomScaled(0.75), entity.getRandomZ(0.75), random.x, random.y, random.z);
        }
    }
}
