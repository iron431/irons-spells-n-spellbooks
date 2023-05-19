package io.redspace.ironsspellbooks.spells.poison;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.effect.SpiderAspectEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.holy.HealSpell;
import io.redspace.ironsspellbooks.util.AnimationHolder;
import io.redspace.ironsspellbooks.util.Utils;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;


public class SpiderAspectSpell extends AbstractSpell {
    public SpiderAspectSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.additional_poisoned_damage", Utils.stringTruncation(getPercentDamage(), 0)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(caster) * 20, 1))
        );
    }

    public SpiderAspectSpell(int level) {
        super(SpellType.SPIDER_ASPECT_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 5;
        this.castTime = 0;
        this.baseManaCost = 35;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SPIDER_ASPECT_CAST.get());
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.SPIDER_ASPECT.get(), (int) (getSpellPower(entity) * 20), this.level - 1, false, false, true));

        super.onCast(level, entity, playerMagicData);
    }

    private float getPercentDamage() {
        return level * SpiderAspectEffect.DAMAGE_PER_LEVEL * 100;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return HealSpell.SELF_CAST_ANIMATION;
    }
}
