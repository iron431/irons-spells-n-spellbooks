package com.example.testmod.spells.ender;

import com.example.testmod.capabilities.magic.CastData;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.network.spell.ClientboundTeleportParticles;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class TeleportSpell extends AbstractSpell {

    public TeleportSpell() {
        this(1);
    }

    public TeleportSpell(int level) {
        super(SpellType.TELEPORT_SPELL);
        this.level = level;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 6;
        this.baseManaCost = 15;
        this.manaCostPerLevel = 3;
        this.cooldown = 200;
        this.castTime = 0;
        uniqueInfo.add(Component.translatable("ui.testmod.distance", Utils.stringTruncation(getDistance(null), 1)));

    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, PlayerMagicData playerMagicData) {
        particleCloud(level, entity, entity.getPosition(1));

        Vec3 dest = null;

        if (playerMagicData != null) {
            if (playerMagicData.getAdditionalCastData() instanceof TeleportData teleportData) {
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
        return Optional.of(SoundEvents.ILLUSIONER_MIRROR_MOVE);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        var teleportData = (TeleportData) playerMagicData.getAdditionalCastData();

        Vec3 dest = null;
        if (teleportData != null) {
            var potentialTarget = teleportData.getTeleportTargetPosition();
            if (potentialTarget != null) {
                dest = findTeleportLocation(entity, potentialTarget);
            }
        }

        if (dest == null) {
            dest = findTeleportLocation(level, entity);
        }

        Messages.sendToPlayersTrackingEntity(new ClientboundTeleportParticles(entity.position(), dest), entity);
        entity.teleportTo(dest.x, dest.y, dest.z);
        entity.resetFallDistance();

        playerMagicData.resetAdditionalCastData();

        super.onCast(level, entity, playerMagicData);
    }

    public Vec3 findTeleportLocation(LivingEntity entity, Vec3 location) {
        int y = entity.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) location.x, (int) location.z);
        return new Vec3(location.x, y, location.z);
    }

    private Vec3 findTeleportLocation(Level level, LivingEntity entity) {
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.ANY, getDistance(entity));
        var pos = blockHitResult.getBlockPos();

        Vec3 bbOffset = entity.getForward().normalize().multiply(entity.getBbWidth() / 3, 0, entity.getBbHeight() / 3);
        Vec3 rawImpact = blockHitResult.getLocation().subtract(bbOffset);
        int ledgeY = entity.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        Vec3 correctedPos = new Vec3(pos.getX(), ledgeY, pos.getZ());
        boolean los = level.clip(new ClipContext(rawImpact, rawImpact.add(0, ledgeY - pos.getY(), 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, entity)).getType() == HitResult.Type.MISS;

        if (los && Math.abs(ledgeY - pos.getY()) <= 3) {
            return correctedPos.add(0.5, 0, 0.5);
        } else {
            Vec3 anchoredImpact = level.clip(new ClipContext(rawImpact, rawImpact.add(0, -entity.getEyeHeight(), 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, entity)).getLocation();
            return anchoredImpact;
        }

    }

    public static void particleCloud(Level level, LivingEntity entity, Vec3 pos) {
        if (level.isClientSide) {
            double width = 0.5;
            float height = entity.getBbHeight() / 2;
            for (int i = 0; i < 55; i++) {
                double x = pos.x + level.random.nextDouble() * width * 2 - width;
                double y = pos.y + height + level.random.nextDouble() * height * 1.2 * 2 - height * 1.2;
                double z = pos.z + level.random.nextDouble() * width * 2 - width;
                double dx = level.random.nextDouble() * .1 * (level.random.nextBoolean() ? 1 : -1);
                double dy = level.random.nextDouble() * .1 * (level.random.nextBoolean() ? 1 : -1);
                double dz = level.random.nextDouble() * .1 * (level.random.nextBoolean() ? 1 : -1);
                level.addParticle(ParticleTypes.PORTAL, true, x, y, z, dx, dy, dz);
            }
        }
    }

    private float getDistance(Entity sourceEntity) {
        return getSpellPower(sourceEntity);
    }

    public static class TeleportData implements CastData {
        private Vec3 teleportTargetPosition;

        public TeleportData(Vec3 teleportTargetPosition) {
            this.teleportTargetPosition = teleportTargetPosition;
        }

        public void setTeleportTargetPosition(Vec3 targetPosition) {
            this.teleportTargetPosition = targetPosition;
        }

        public Vec3 getTeleportTargetPosition() {
            return this.teleportTargetPosition;
        }

        @Override
        public void reset() {
            //Nothing needed here for teleport
        }
    }

}
