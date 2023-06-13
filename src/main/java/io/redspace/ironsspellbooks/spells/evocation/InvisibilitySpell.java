package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class InvisibilitySpell extends AbstractSpell {
    public InvisibilitySpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(caster) * 20, 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.EVOCATION)
            .setMaxLevel(6)
            .setCooldownSeconds(45)
            .build();

    public InvisibilitySpell(int level) {
        super(SpellType.INVISIBILITY_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 40;
        this.baseManaCost = 35;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_MIRROR);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY.get(), getDuration(entity), 0, false, false, true));

        var targetingCondition = TargetingConditions.forCombat().selector(e -> {
            IronsSpellbooks.LOGGER.debug("InvisibilitySpell TargetingConditions:{}", e);
            return (((Mob) e).getTarget() == entity);
        });

        world.getNearbyEntities(Mob.class, targetingCondition, entity, entity.getBoundingBox().inflate(40D))
                .forEach(entityTargetingCaster -> {
                    IronsSpellbooks.LOGGER.debug("InvisibilitySpell Clear Target From:{}", entityTargetingCaster);
                    entityTargetingCaster.setTarget(null);
                    entityTargetingCaster.setLastHurtMob(null);
                    entityTargetingCaster.setLastHurtByMob(null);
                    entityTargetingCaster.targetSelector.getAvailableGoals().forEach(WrappedGoal::stop);
                });

        super.onCast(world, entity, playerMagicData);
    }

    private int getDuration(LivingEntity source) {
        return (int) (getSpellPower(source) * 20);
    }

}
