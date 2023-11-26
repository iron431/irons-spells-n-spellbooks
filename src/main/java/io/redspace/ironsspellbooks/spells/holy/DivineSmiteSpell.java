package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class DivineSmiteSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "divine_smite");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public DivineSmiteSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 3;
        this.castTime = 16;
        this.baseManaCost = 30;
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        //due to melee animation timing, we do not want cast time attribute to affect this spell
        return getCastTime(spellLevel);
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
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.DIVINE_SMITE_WINDUP.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.DIVINE_SMITE_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        float radius = 1.75f;
        Vec3 smiteLocation = Utils.moveToRelativeGroundLevel(level, entity.getEyePosition().add(entity.getForward().multiply(1, 0, 1).normalize().scale(1.35f)), 2);
        MagicManager.spawnParticles(level, new ShockwaveParticleOptions(SchoolRegistry.HOLY.get().getTargetingColor(), radius * 2), smiteLocation.x, smiteLocation.y + .15f, smiteLocation.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, ParticleTypes.ELECTRIC_SPARK, smiteLocation.x, smiteLocation.y + .15f, smiteLocation.z, 50, 0, 0, 0, 1, false);
        CameraShakeManager.addCameraShake(new CameraShakeData(10, smiteLocation, 10));
        var entities = level.getEntities(entity, AABB.ofSize(smiteLocation, radius * 2, radius * 4, radius * 2));
        for (Entity targetEntity : entities) {
            //double distance = targetEntity.distanceToSqr(smiteLocation);
            if (/*distance < radius * radius && */Utils.hasLineOfSight(level, smiteLocation, targetEntity.getBoundingBox().getCenter(), true)) {
                DamageSources.applyDamage(targetEntity, getDamage(spellLevel, entity), this.getDamageSource(entity));
            }
        }
        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        //IronsSpellbooks.LOGGER.debug("SmitingStrikeSpell.getDamage {} + {} + {}", getSpellPower(spellLevel, entity), base, enchant);
        return getSpellPower(spellLevel, entity) + getWeaponDamage(entity);
    }

    private float getWeaponDamage(LivingEntity entity) {
        if (entity != null) {
            float weapon = (float) (entity.getAttributeValue(Attributes.ATTACK_DAMAGE));
            float fist = (float) (entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            if (weapon == fist) {
                //Remove fist damage if they are not using a melee weapon
                weapon -= fist;
            }
            //Setting mob type to undead means the smite enchantment also adds to the spell's damage. Seems fitting.
            float enchant = EnchantmentHelper.getDamageBonus(entity.getMainHandItem(), MobType.UNDEAD);
            return weapon + enchant;
        }
        return 0;
    }

    private String getDamageText(int spellLevel, LivingEntity entity) {
        if (entity != null) {
            float weaponDamage = getWeaponDamage(entity);
            String plus = "";
            if (weaponDamage > 0) {
                plus = String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1));
            }
            String damage = Utils.stringTruncation(getDamage(spellLevel, entity), 1);
            return damage + plus;
        }
        return "" + getSpellPower(spellLevel, entity);
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
