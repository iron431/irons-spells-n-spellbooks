package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

public class HealingAoe extends AoeEntity implements AntiMagicSusceptible {

    public HealingAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public HealingAoe(Level level) {
        this(EntityRegistry.HEALING_AOE.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        if (getOwner() instanceof LivingEntity owner && Utils.shouldHealEntity(owner, target)) {
            float healAmount = getDamage();
            NeoForge.EVENT_BUS.post(new SpellHealEvent((LivingEntity) getOwner(), target, healAmount, SchoolRegistry.HOLY.get()));
            target.heal(healAmount);
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return !pTarget.isSpectator() && pTarget.isAlive() && pTarget.isPickable();
    }

    @Override
    public float getParticleCount() {
        return 0.35f;
    }

    @Override
    protected float getParticleSpeedModifier() {
        return 0f;
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, 1, 0);
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(ClientSpellCastHelper.coloredMobEffect(MobEffects.HEAL.value().getColor()));//Optional.of(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, MobEffects.HEAL.value().getColor()));
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        discard();
    }
}
