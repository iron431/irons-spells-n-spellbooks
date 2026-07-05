package io.redspace.skillcasting.irons_spellbooks.spells.holy;

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
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;
import java.util.Optional;

public class DivineSmiteSpell extends AbstractSpellSkill {

    private static final float RADIUS = 2.2f;
    private static final float RANGE = 1.7f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public DivineSmiteSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = 16;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float weaponDamage = castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        String plus = weaponDamage > 0 ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(damage + weaponDamage, 1) + plus));
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
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
        return PlayableSound.standard(SoundRegistry.DIVINE_SMITE_WINDUP).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.DIVINE_SMITE_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float weaponDamage = 0;
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            weaponDamage = getAdditionalDamage(entity);
        }
        castContext.set(SkillcastingComponentTypes.WEAPON_DAMAGE, weaponDamage);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        Vec3 forward = castContext.direction();
        Vec3 eyePos = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 smiteLocation = Utils.raycastForBlock(level, eyePos,
                eyePos.add(forward.multiply(RANGE, 0, RANGE)), ClipContext.Fluid.NONE).getLocation();
        Vec3 particleLocation = level.clip(new ClipContext(smiteLocation, smiteLocation.add(0, -2, 0),
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation().add(0, 0.1, 0);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(getSchoolType().getTargetingColor(), RADIUS * 2),
                particleLocation.x, particleLocation.y, particleLocation.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, ParticleTypes.ELECTRIC_SPARK,
                particleLocation.x, particleLocation.y, particleLocation.z, 50, 0, 0, 0, 1, false);
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, particleLocation, 10));

        float totalDamage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f)
                + castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        var damageSource = getDamageSource(level, caster);
        var entities = level.getEntities(caster, AABB.ofSize(smiteLocation, RADIUS * 2, RADIUS * 4, RADIUS * 2));
        for (Entity targetEntity : entities) {
            if (targetEntity.isAlive() && targetEntity.isPickable()
                    && Utils.hasLineOfSight(level, smiteLocation.add(0, 1, 0), targetEntity.getBoundingBox().getCenter(), true)) {
                if (DamageSources.applyDamage(targetEntity, totalDamage, damageSource)
                        && level instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, targetEntity, damageSource);
                }
            }
        }
    }

    private static float getAdditionalDamage(LivingEntity entity) {
        float weaponDamage = Utils.getWeaponDamage(entity);
        var weaponItem = entity.getWeaponItem();
        if (!weaponItem.isEmpty() && weaponItem.has(DataComponents.ENCHANTMENTS)) {
            weaponDamage += Utils.processEnchantment(entity.level(), Enchantments.SMITE, EnchantmentEffectComponents.DAMAGE,
                    weaponItem.get(DataComponents.ENCHANTMENTS));
        }
        return weaponDamage;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.OVERHEAD_MELEE_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }
}
