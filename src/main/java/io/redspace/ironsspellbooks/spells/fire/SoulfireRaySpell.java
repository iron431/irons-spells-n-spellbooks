package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.SoulfireRayParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SoulfireRaySpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "soulfire_ray");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.reduced_healing", Utils.timeFromTicks(getFreezeTime(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(spellLevel, caster), 1))
        );
    }

    public SoulfireRaySpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 5;
        this.baseManaCost = 50;
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
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_RAY_CHARGE;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ONE_HANDED_RAY_SHOOT;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SOULFIRE_RAY_CAST.value());
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.SOULFIRE_RAY_CHARGE.value());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var hitResult = Utils.raycastForEntity(level, entity, getRange(spellLevel, entity), true, .15f);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            DamageSources.applyDamage(target, getDamage(spellLevel, entity), getDamageSource(entity));
            //todo: soul burn
        }
        Vec3 origin = entity.getEyePosition().add(entity.getForward().scale(0.5).subtract(0, 0.4, 0));
        MagicManager.spawnParticles(level, new SoulfireRayParticleOptions(hitResult.getLocation().subtract(hitResult.getLocation().subtract(origin).normalize().scale(0.25))), origin.x, origin.y, origin.z, 1, 0, 0, 0, 0, true);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static float getRange(int level, LivingEntity caster) {
        return 30;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return 3 + getSpellPower(spellLevel, caster) * 1.5f;
    }

    private int getFreezeTime(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 15);
    }
}
