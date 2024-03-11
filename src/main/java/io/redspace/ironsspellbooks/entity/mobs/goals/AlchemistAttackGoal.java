package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.A;
import org.ietf.jgss.Oid;

import java.util.List;
import java.util.function.Supplier;

public class AlchemistAttackGoal extends WizardAttackGoal {

    protected float throwRangeSqr;
    protected float throwRange;
    protected float potionBias;

    public AlchemistAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval, float throwRange, float potionBias) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval);
        this.throwRange = throwRange;
        this.throwRangeSqr = throwRange * throwRange;
        this.potionBias = potionBias;
    }

    @Override
    public AlchemistAttackGoal setSpells(List<AbstractSpell> attackSpells, List<AbstractSpell> defenseSpells, List<AbstractSpell> movementSpells, List<AbstractSpell> supportSpells) {
        return (AlchemistAttackGoal) super.setSpells(attackSpells, defenseSpells, movementSpells, supportSpells);
    }

    @Override
    public AlchemistAttackGoal setSpellQuality(float minSpellQuality, float maxSpellQuality) {
        return (AlchemistAttackGoal) super.setSpellQuality(minSpellQuality, maxSpellQuality);
    }

    @Override
    public AlchemistAttackGoal setSingleUseSpell(AbstractSpell spellType, int minDelay, int maxDelay, int minLevel, int maxLevel) {
        return (AlchemistAttackGoal) super.setSingleUseSpell(spellType, minDelay, maxDelay, minLevel, maxLevel);
    }

    @Override
    public AlchemistAttackGoal setIsFlying() {
        return (AlchemistAttackGoal) super.setIsFlying();
    }

    private static final List<MobEffect> ATTACK_POTIONS = List.of(MobEffects.POISON, MobEffects.WEAKNESS, MobEffects.BLINDNESS, MobEffects.LEVITATION, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN);

    @Override
    protected void doSpellAction() {
        //Instead of taking a normal casting action, there is a chance to attempt a potion action
        if (this.mob.distanceToSqr(this.target) < throwRangeSqr && this.mob.getRandom().nextFloat() < potionBias) {
            int attackWeight = getAttackWeight();
            int supportWeight = getSupportWeight();
            ((ServerLevel) this.mob.level).players().forEach(player -> player.sendSystemMessage(Component.literal("attack:  " + attackWeight)));
            ((ServerLevel) this.mob.level).players().forEach(player -> player.sendSystemMessage(Component.literal("support: " + supportWeight)));
            ItemStack potion = new ItemStack(Items.SPLASH_POTION);

            var targetedEntity = target;
            if (attackWeight > supportWeight) {
                // We want the potion amplifier to scale with the "difficulty" of our target, with a chunk of randomness. For vanilla players, this will stick to I-II.
                // For modded players or entities with way more difficulty (health), we'll add a randomized factor based on their health above normal (20)
                int amplifier = (int) ((mob.getRandom().nextFloat() < 0.75f ? 0 : 1) + Math.max((target.getMaxHealth() - 20) / mob.getRandom().nextIntBetweenInclusive(7, 12), 0));
                MobEffect effect = target.isInvertedHealAndHarm() ? MobEffects.HEAL : MobEffects.HARM;
                if (mob.getRandom().nextFloat() < 0.6f) {
                    //Effect, instead of damage
                    for (int i = 0; i < ATTACK_POTIONS.size(); i++) {
                        int p = mob.getRandom().nextInt(ATTACK_POTIONS.size());
                        if (!target.hasEffect(ATTACK_POTIONS.get(p))) {
                            effect = ATTACK_POTIONS.get(p);
                            ((ServerLevel) this.mob.level).players().forEach(player -> player.sendSystemMessage(Component.literal("effect: " + ATTACK_POTIONS.get(p).getDescriptionId())));
                            ((ServerLevel) this.mob.level).players().forEach(player -> player.sendSystemMessage(Component.literal("amp: " + amplifier)));
                            break;
                        }
                    }
                }
                PotionUtils.setCustomEffects(potion, List.of(new MobEffectInstance(effect, effect.isInstantenous() ? 0 : 200, amplifier)));
                PotionUtils.setPotion(potion, Potions.WATER);
            } else {
                PotionUtils.setPotion(potion, Potions.STRONG_HEALING);
                targetedEntity = this.mob;
            }
            ThrownPotion thrownpotion = new ThrownPotion(this.mob.level, this.mob);
            thrownpotion.setItem(potion);
            thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
            Vec3 vec3 = targetedEntity.getDeltaMovement();
            double d0 = targetedEntity.getX() + vec3.x - this.mob.getX();
            double d1 = targetedEntity.getEyeY() - (double) 1.1F - this.mob.getEyeY();
            double d2 = targetedEntity.getZ() + vec3.z - this.mob.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            thrownpotion.shoot(d0, d1 + d3 * 0.2D, d2, (float) Mth.clampedLerp(0.5f, 1.25f, this.mob.distanceToSqr(targetedEntity) / throwRangeSqr), 8.0F);
            if (!this.mob.isSilent()) {
                this.mob.level.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(), SoundEvents.WITCH_THROW, this.mob.getSoundSource(), 1.0F, 0.8F + this.mob.getRandom().nextFloat() * 0.4F);
            }
            this.mob.level.addFreshEntity(thrownpotion);

            return;
        }
        super.doSpellAction();
    }
}
