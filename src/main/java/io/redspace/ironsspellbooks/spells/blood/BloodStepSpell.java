package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.util.AnimationHolder;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BloodStepSpell extends AbstractSpell {
    public BloodStepSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getDistance(caster), 1)));
    }

    public BloodStepSpell(int level) {
        super(SpellType.BLOOD_STEP_SPELL);
        this.level = level;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 4;
        this.baseManaCost = 30;
        this.manaCostPerLevel = 10;
        this.castTime = 0;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.BLOOD_STEP.get());
    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        super.onClientPreCast(level, entity, hand, playerMagicData);
        Vec3 forward = entity.getForward().normalize();
        for (int i = 0; i < 35; i++) {
            Vec3 motion = forward.scale(level.random.nextDouble() * .25f);
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, entity.getRandomX(.4f), entity.getRandomY(), entity.getRandomZ(.4f), motion.x, motion.y, motion.z);
        }
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        Vec3 dest = null;
        var teleportData = (TeleportSpell.TeleportData) playerMagicData.getAdditionalCastData();
        if (teleportData != null) {
            var potentialTarget = teleportData.getTeleportTargetPosition();
            if (potentialTarget != null) {
                dest = potentialTarget;
                entity.teleportTo(dest.x, dest.y, dest.z);
            }
        }else{
            HitResult hitResult = Utils.raycastForEntity(level, entity, getDistance(entity), true);
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            if (hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult) hitResult).getEntity() instanceof LivingEntity target) {
                //dest = target.position().subtract(new Vec3(0, 0, 1.5).yRot(target.getYRot()));
                for (int i = 0; i < 8; i++) {
                    dest = target.position().subtract(new Vec3(0, 0, 1.5).yRot(-(target.getYRot() + i * 45) * Mth.DEG_TO_RAD));
                    if (level.getBlockState(new BlockPos(dest).above()).isAir())
                        break;

                }
                entity.teleportTo(dest.x, dest.y + 1f, dest.z);
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition().subtract(0, .15, 0));
            } else {
                dest = TeleportSpell.findTeleportLocation(level, entity, getDistance(entity));
                entity.teleportTo(dest.x, dest.y, dest.z);

            }
        }
        entity.resetFallDistance();
        level.playSound(null, dest.x, dest.y, dest.z, getCastFinishSound().get(), SoundSource.NEUTRAL, 1f, 1f);

        //Invis take 1 tick to set in
        entity.setInvisible(true);
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY.get(), 100, 0, false, false, true));


        super.onCast(level, entity, playerMagicData);
    }

    private float getDistance(Entity sourceEntity) {
        return getSpellPower(sourceEntity);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return AnimationHolder.none();
    }

}
