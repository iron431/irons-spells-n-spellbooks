package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.FlameStrikeParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class FlamingStrikeSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public FlamingStrikeSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 2;
        this.castTime = 10;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float weaponDamage = castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        String plus = weaponDamage > 0 ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(damage + weaponDamage, 1) + plus));
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
        return PlayableSound.standard(SoundRegistry.FLAMING_STRIKE_UPSWING).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.FLAMING_STRIKE_SWING).toOpt();
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) {
        return false;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_TIME, castTime);
        float weaponDamage = 0;
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            weaponDamage = Utils.getWeaponDamage(entity);
            var weaponItem = entity.getWeaponItem();
            if (!weaponItem.isEmpty() && weaponItem.has(DataComponents.ENCHANTMENTS)) {
                weaponDamage += Utils.getEnchantmentLevel(entity.level(), Enchantments.FIRE_ASPECT, weaponItem.get(DataComponents.ENCHANTMENTS));
            }
        }
        castContext.set(SkillcastingComponentTypes.WEAPON_DAMAGE, weaponDamage);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        float radius = 3.25f;
        float distance = 1.9f;
        Vec3 forward = castContext.direction();
        Vec3 castOrigin = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 hitLocation = castContext.position(PositionAnchor.CENTER).add(forward.scale(distance));
        var entities = level.getEntities(castContext.asEntityCaster(), AABB.ofSize(hitLocation, radius * 2, radius, radius * 2));
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f) + castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        var damageSource = getDamageSource(level, castContext.asEntityCaster());
        for (Entity targetEntity : entities) {
            if (targetEntity instanceof LivingEntity living && living.isAlive() && living.isPickable() &&
                    living.position().subtract(castOrigin).dot(forward) >= 0 &&
                    castOrigin.distanceToSqr(living.position()) < radius * radius &&
                    Utils.hasLineOfSight(level, castOrigin, living.getBoundingBox().getCenter(), true) &&
                    living.getBoundingBox().getCenter().subtract(castOrigin).dot(forward) >= 0) {
                if (DamageSources.applyDamage(living, damage, damageSource)) {
                    MagicManager.spawnParticles(level, ParticleHelper.FIRE, living.getX(), living.getY() + living.getBbHeight() * 0.5f, living.getZ(),
                            30, living.getBbWidth() * 0.5f, living.getBbHeight() * 0.5f, living.getBbWidth() * 0.5f, 0.03, false);
                    EnchantmentHelper.doPostAttackEffects((ServerLevel) level, living, damageSource);
                }
            }
        }
        boolean mirrored = SkillSelectionManager.OFFHAND.equals(castContext.getOrNull(SkillcastingComponentTypes.CAST_SOURCE));
        MagicManager.spawnParticles(level, new FlameStrikeParticleOptions((float) forward.x, (float) forward.y, (float) forward.z, mirrored, false, 1f),
                hitLocation.x, hitLocation.y + 0.5, hitLocation.z, 1, 0, 0, 0, 0, true);
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setFireTicks(60);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }
}
