package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TrialOmenEffect extends MagicMobEffect implements ISyncedMobEffect {
    public TrialOmenEffect() {
        super(MobEffectCategory.NEUTRAL, 1484454);
    }

    @Override
    public void clientTick(LivingEntity livingEntity, MobEffectInstance instance) {
        var random = livingEntity.getRandom();
        if (random.nextFloat() < 0.5f) {
            Vec3 motion = new Vec3(
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1,
                    random.nextFloat() * 2 - 1
            );
            motion = motion.scale(.03f).add(livingEntity.getDeltaMovement().scale(1)).add(0, 0.04, 0);
            livingEntity.level.addParticle(ParticleHelper.TRIAL_OMEN, livingEntity.getRandomX(.25f), livingEntity.getRandomY(), livingEntity.getRandomZ(.25f), motion.x, motion.y + 0.1, motion.z);
        }
    }
}
