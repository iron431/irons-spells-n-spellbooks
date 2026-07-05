package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.effect.ThunderstormEffect;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ThunderstormSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "thunderstorm");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(ThunderstormEffect.getDamageFromAmplifier(getAmplifierForLevel(spellLevel, caster), caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", 20),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(120)
            .build();

    public ThunderstormSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 70;
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
        return Optional.of(SoundRegistry.THUNDERSTORM_PREPARE.value());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.THUNDERSTORM, getDurationTicks(spellLevel, entity), getAmplifierForLevel(spellLevel, entity), false, false, true));
        int count = 3;
        for (int i = 0; i < count; i++) {
            Vec3 offset = new Vec3(0, 5 + level.getRandom().nextFloat() * 2, 2 + level.getRandom().nextFloat());
            offset = offset.yRot(i * Mth.TWO_PI / count);
            Vec3 location = entity.position().add(offset);
            MagicManager.spawnParticles(level, ParticleHelper.FOG_THUNDER_LIGHT, location.x, location.y, location.z, 2, 1, 1, 1, 1, true);
            MagicManager.spawnParticles(level, ParticleHelper.FOG_THUNDER_DARK, location.x, location.y, location.z, 2, 1, 1, 1, 1, true);
            MagicManager.spawnParticles(level, new ZapParticleOption(location), entity.getX(), entity.getY(), entity.getZ(), 1, 1,0,1,0, true);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private int getAmplifierForLevel(int spellLevel, LivingEntity caster) {
        return 8 + (int) ((spellLevel - 1) * getEntityPowerMultiplier(caster));
    }

    public int getDurationTicks(int spellLevel, LivingEntity caster) {
        return (int) ((20 + (2 * (spellLevel - 1) * getEntityPowerMultiplier(caster))) * 20);
    }
}
