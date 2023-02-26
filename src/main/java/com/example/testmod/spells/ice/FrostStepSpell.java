package com.example.testmod.spells.ice;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.mobs.frozen_humanoid.FrozenHumanoid;
import com.example.testmod.registries.SoundRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.TeleportSpell;
import com.example.testmod.util.ParticleHelper;
import com.example.testmod.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FrostStepSpell extends AbstractSpell {

    public FrostStepSpell() {
        this(1);
    }

    public FrostStepSpell(int level) {
        super(SpellType.FROST_STEP);
        this.level = level;
        this.baseSpellPower = 14;
        this.spellPowerPerLevel = 3;
        this.baseManaCost = 15;
        this.manaCostPerLevel = 3;
        this.cooldown = 200;
        this.castTime = 0;
        uniqueInfo.add(Component.translatable("ui.testmod.distance", Utils.stringTruncation(getDistance(null), 1)));
        uniqueInfo.add(Component.translatable("ui.testmod.shatter_damage", Utils.stringTruncation(getDamage(null), 1)));

    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, PlayerMagicData playerMagicData) {
        particleCloud(level, entity, entity.getPosition(1));

        Vec3 dest = null;

        if (playerMagicData != null) {
            if (playerMagicData.getAdditionalCastData() instanceof TeleportSpell.TeleportData teleportData) {
                var tmp = teleportData.getTeleportTargetPosition();
                int y = entity.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) tmp.x, (int) tmp.z);
                dest = new Vec3(tmp.x, y, tmp.z);
            }
        }

        if (dest == null) {
            dest = findTeleportLocation(level, entity);
        }

        particleCloud(level, entity, dest);
        super.onClientPreCast(level, entity, hand, playerMagicData);
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.FROST_STEP.get());
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        var teleportData = (TeleportSpell.TeleportData) playerMagicData.getAdditionalCastData();

        FrozenHumanoid shadow = new FrozenHumanoid(level, entity);
        shadow.setShatterDamage(getDamage(entity));
        shadow.setDeathTimer(60);
        level.addFreshEntity(shadow);
        Vec3 dest = null;
        if (teleportData != null) {
            var potentialTarget = teleportData.getTeleportTargetPosition();
            if (potentialTarget != null) {
                dest = Utils.putVectorOnWorldSurface(level, potentialTarget);
            }
        }

        if (dest == null) {
            dest = findTeleportLocation(level, entity);
        }

        entity.teleportTo(dest.x, dest.y, dest.z);
        entity.resetFallDistance();
        level.playSound(null, dest.x, dest.y, dest.z, getCastFinishSound().get(), SoundSource.NEUTRAL, 1f, 1f);

        playerMagicData.resetAdditionalCastData();

        super.onCast(level, entity, playerMagicData);
    }

    private Vec3 findTeleportLocation(Level level, LivingEntity entity) {
        return TeleportSpell.findTeleportLocation(level, entity, getDistance(entity));
    }

    public static void particleCloud(Level level, LivingEntity entity, Vec3 pos) {
        if (level.isClientSide) {
            double width = 0.5;
            float height = entity.getBbHeight() / 2;
            for (int i = 0; i < 25; i++) {
                double x = pos.x + level.random.nextDouble() * width * 2 - width;
                double y = pos.y + height + level.random.nextDouble() * height * 1.2 * 2 - height * 1.2;
                double z = pos.z + level.random.nextDouble() * width * 2 - width;
                double dx = level.random.nextDouble() * .1 * (level.random.nextBoolean() ? 1 : -1);
                double dy = level.random.nextDouble() * .1 * (level.random.nextBoolean() ? 1 : -1);
                double dz = level.random.nextDouble() * .1 * (level.random.nextBoolean() ? 1 : -1);
                level.addParticle(ParticleHelper.SNOWFLAKE, true, x, y, z, dx, dy, dz);
                level.addParticle(ParticleTypes.SNOWFLAKE, true, x, y, z, -dx, -dy, -dz);
            }
        }
    }

    private float getDistance(Entity sourceEntity) {
        return getSpellPower(sourceEntity) * .65f;
    }

    private float getDamage(Entity caster) {
        return this.getSpellPower(caster) / 3;
    }

}
