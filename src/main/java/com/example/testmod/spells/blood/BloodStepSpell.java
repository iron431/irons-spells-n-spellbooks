package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.util.Utils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
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

import java.util.Optional;

public class BloodStepSpell extends AbstractSpell {
    public BloodStepSpell() {
        this(1);
    }

    public BloodStepSpell(int level) {
        super(SpellType.BLOOD_STEP_SPELL);
        this.level = level;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 2;
        this.baseManaCost = 15;
        this.manaCostPerLevel = 3;
        this.cooldown = 200;
        this.castTime = 0;
        uniqueInfo.add(Component.translatable("ui.testmod.distance", Utils.stringTruncation(getDistance(null), 1)));


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
        HitResult hitResult = Utils.raycastForEntity(level, entity, getDistance(entity), true);
        Vec3 dest = null;
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
        entity.resetFallDistance();


        //Invis take 1 tick to set in
        entity.setInvisible(true);
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY.get(), 100, 0, false, false, true));


        super.onCast(level, entity, playerMagicData);
    }

    private float getDistance(Entity sourceEntity) {
        return getSpellPower(sourceEntity);
    }

}
