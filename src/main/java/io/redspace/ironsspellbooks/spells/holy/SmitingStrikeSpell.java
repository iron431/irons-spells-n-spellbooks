package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SmitingStrikeSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "smiting_strike");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public SmitingStrikeSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 3;
        this.castTime = 16;
        this.baseManaCost = 15;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        //due to melee animation, we do not want cast time attribute to affect this spell
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
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_MIRROR);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.EVOCATION_CAST.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        float explosionRadius = 1.5f;
        Vec3 smiteLocation = Utils.moveToRelativeGroundLevel(level, entity.getEyePosition().add(entity.getForward().multiply(1, 0, 1).normalize().scale(2.25f)), 3);
        //TODO: particle packet
        MagicManager.spawnParticles(level, ParticleHelper.BLOOD, smiteLocation.x, smiteLocation.y, smiteLocation.z, 100, 0, 0, 0, 0.5, true);
        var entities = level.getEntities(entity, AABB.ofSize(smiteLocation, explosionRadius * 2, explosionRadius * 4, explosionRadius * 2));
        for (Entity targetEntity : entities) {
            double distance = targetEntity.distanceToSqr(smiteLocation);
            if (distance < explosionRadius * explosionRadius && Utils.hasLineOfSight(level, smiteLocation, targetEntity.getBoundingBox().getCenter(), true)) {
                DamageSources.applyDamage(targetEntity, getDamage(spellLevel, entity), this.getDamageSource(entity));
            }
        }
        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        //TODO: the client is not aware of the attributes of the entity...
        float base = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float enchant = EnchantmentHelper.getDamageBonus(entity.getMainHandItem(), MobType.UNDEFINED);

        IronsSpellbooks.LOGGER.debug("SmitingStrikeSpell.getDamage {} + {} + {}", getSpellPower(spellLevel, entity), base, enchant);
        return getSpellPower(spellLevel, entity) + base + enchant;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SMITING_STRIKE_ANIMATION;
    }
}
