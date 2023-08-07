package io.redspace.ironsspellbooks.spells.poison;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.effect.BlightEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class BlightSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "blight");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.reduced_healing", Utils.stringTruncation((1 + getAmplifier(spellLevel, caster)) * BlightEffect.HEALING_PER_LEVEL * -100, 1)),
                Component.translatable("ui.irons_spellbooks.reduced_damage", Utils.stringTruncation((1 + getAmplifier(spellLevel, caster)) * BlightEffect.DAMAGE_PER_LEVEL * -100, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.NATURE)
            .setMaxLevel(8)
            .setCooldownSeconds(35)
            .build();

    public BlightSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 50;
        this.baseManaCost = 10;
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
        return Optional.of(SoundRegistry.BLIGHT_BEGIN.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }


    @Override
    public boolean checkPreCastConditions(Level level, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, .35f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof CastTargetingData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            if (targetEntity != null) {
                targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.BLIGHT.get(), getDuration(spellLevel, entity), getAmplifier(spellLevel, entity)));
            }
        }

        super.onCast(world, spellLevel, entity, playerMagicData);
    }

    public int getAmplifier(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * this.getLevel(spellLevel, caster) - 1);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20 * 30);
    }

}
