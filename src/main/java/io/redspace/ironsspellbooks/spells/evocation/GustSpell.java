package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.effect.AirborneEffect;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.gust.GustCollider;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.AnimationHolder;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.Animation;

import java.util.List;
import java.util.Optional;

public class GustSpell extends AbstractSpell {

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.strength", Utils.stringTruncation(getStrength(caster), 1)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(AirborneEffect.getDamageFromLevel(getLevel(caster)), 1))
        );
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchool(SchoolType.EVOCATION)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public GustSpell(int level) {
        super(SpellType.GUST_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 30;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.GUST_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.GUST_CAST.get());
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        float range = getRange(entity);
        float strength = getStrength(entity);

        GustCollider gust = new GustCollider(level, entity);
        gust.setPos(entity.position().add(0, entity.getEyeHeight() * .7, 0).add(entity.getForward().normalize().scale(2f)));
        gust.range = range;
        gust.strength = strength;
        gust.amplifier = this.getLevel(entity) - 1;
        level.addFreshEntity(gust);
        gust.setDealDamageActive();
        gust.tick();

        super.onCast(level, entity, playerMagicData);
    }

    public float getRange(LivingEntity caster) {
        return 8;
    }

    public float getStrength(LivingEntity caster) {
        return getSpellPower(caster) * .2f;
    }

    public float getDamage(LivingEntity caster) {
        return getSpellPower(caster);
    }

    public static final AnimationHolder CHARGE_WAVY_ANIMATION = new AnimationHolder("charge_wavy", Animation.LoopType.PLAY_ONCE);

    @Override
    public AnimationHolder getCastStartAnimation() {
        return CHARGE_WAVY_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AbstractSpell.ANIMATION_LONG_CAST_FINISH;
    }

}
