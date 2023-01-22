package com.example.testmod.spells.ender;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TeleportSpell extends AbstractSpell {

    public TeleportSpell() {
        this(1);
    }

    public TeleportSpell(int level) {
        super(SpellType.TELEPORT_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 32;
        this.spellPowerPerLevel = 10;
        this.baseManaCost = 35;
        this.cooldown = 200;
        this.castTime = 20;

        //TODO: remove these after tsting
        this.baseManaCost = 1;
        this.cooldown = 0;
        this.castTime = 0;

    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
        particleCloud(level, entity, entity.getPosition(1));

        Vec3 dest = null;

        if (playerMagicData != null && playerMagicData.getTeleportTargetPosition() != null) {
            var tmp = playerMagicData.getTeleportTargetPosition();
            int y = entity.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) tmp.x, (int) tmp.z);
            dest = new Vec3(tmp.x, y, tmp.z);
        } else {
            dest = findTeleportLocation(level, entity);
        }

        particleCloud(level, entity, dest);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
        var potentialTarget = playerMagicData.getTeleportTargetPosition();
        var dest = potentialTarget != null ? findTeleportLocation(entity, potentialTarget) : findTeleportLocation(level, entity);

        entity.teleportTo(dest.x, dest.y, dest.z);
        entity.resetFallDistance();
        playerMagicData.setTeleportTargetPosition(null);
    }

    public Vec3 findTeleportLocation(LivingEntity entity, Vec3 location) {
        int y = entity.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) location.x, (int) location.z);
        return new Vec3(location.x, y, location.z);
    }

    private Vec3 findTeleportLocation(Level level, LivingEntity entity) {
        //TODO: potentially cache this result
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.ANY, getDistance(entity));
        var pos = blockHitResult.getBlockPos();

        //TODO: if this is a performance hit can just manually check the few blocks over this position
        int y = entity.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());

        if (y - pos.getY() > 3 || blockHitResult.getType() == HitResult.Type.MISS) {
            return blockHitResult.getLocation();
        } else {
            return new Vec3(pos.getX(), y, pos.getZ());
        }
    }

    private void particleCloud(Level level, LivingEntity entity, Vec3 pos) {
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

    @Override
    public MutableComponent getUniqueInfo() {
        return Component.translatable("ui.testmod.distance", Utils.stringTruncation(getDistance(null), 1));
    }
}
