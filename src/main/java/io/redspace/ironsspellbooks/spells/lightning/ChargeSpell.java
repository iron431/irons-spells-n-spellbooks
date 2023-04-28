package io.redspace.ironsspellbooks.spells.lightning;

import com.mojang.datafixers.util.Either;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.effect.ChargeEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.holy.HealSpell;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.Optional;


public class ChargeSpell extends AbstractSpell {
    public ChargeSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(caster) * 20, 1)),
                Component.translatable("attribute.modifier.plus.1", Utils.stringTruncation(getPercentSpeed(), 0), Component.translatable("attribute.name.generic.movement_speed")),
                Component.translatable("attribute.modifier.plus.1", Utils.stringTruncation(getPercentAttackDamage(), 0), Component.translatable("attribute.name.generic.attack_damage")),
                Component.translatable("attribute.modifier.plus.1", Utils.stringTruncation(getPercentSpellPower(), 0), Component.translatable("attribute.irons_spellbooks.spell_power"))
        );
    }

    public ChargeSpell(int level) {
        super(SpellType.CHARGE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 8;
        this.castTime = 0;
        this.baseManaCost = 50;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.CHARGED.get(), (int) (getSpellPower(entity) * 20), this.level - 1, false, false, true));

        super.onCast(level, entity, playerMagicData);
    }

    private float getPercentAttackDamage() {
        return level * ChargeEffect.ATTACK_DAMAGE_PER_LEVEL * 100;
    }

    private float getPercentSpeed() {
        return level * ChargeEffect.SPEED_PER_LEVEL * 100;
    }

    private float getPercentSpellPower() {
        return level * ChargeEffect.SPELL_POWER_PER_LEVEL * 100;
    }

    @Override
    public Either<AnimationBuilder, ResourceLocation> getCastStartAnimation(Player player) {
        return player == null ? Either.left(HealSpell.ANIMATION_CAST) : Either.right(HealSpell.ANIMATION_CAST_RESOURCE);
    }
}
