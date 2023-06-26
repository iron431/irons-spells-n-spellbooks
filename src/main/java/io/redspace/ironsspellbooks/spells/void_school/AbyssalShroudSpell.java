package io.redspace.ironsspellbooks.spells.void_school;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.spells.holy.HealSpell;
import io.redspace.ironsspellbooks.util.AnimationHolder;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class AbyssalShroudSpell extends AbstractSpell {
    public AbyssalShroudSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(caster) * 20, 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchool(SchoolType.VOID)
            .setMaxLevel(3)
            .setCooldownSeconds(300)
            .build();

    public AbyssalShroudSpell(int level) {
        super(SpellType.ABYSSAL_SHROUD_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 6;
        this.castTime = 0;
        this.baseManaCost = 350;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.ABYSSAL_SHROUD.get());
    }

    @Override
    public void onCast(Level world, LivingEntity entity, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ABYSSAL_SHROUD.get(), (int) getSpellPower(entity) * 20, 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return HealSpell.SELF_CAST_ANIMATION;
    }
}
