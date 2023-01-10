package com.example.testmod.spells.ender;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.particle.ParticleHelper;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
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
    protected void onCast(Level world, Player player, PlayerMagicData playerMagicData) {

        //TestMod.LOGGER.debug("teleport: loc: {}", blockHitResult.getLocation());
        //TestMod.LOGGER.debug("teleport: blockhit: {}, {}, {}", blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ());
        //TestMod.LOGGER.debug("teleport: y: {}", y);
        var dest = findTeleportLocation(world, player);
        player.teleportTo(dest.x, dest.y, dest.z);

        player.resetFallDistance();
    }

    @Override
    public void onClientPreCast(Level level, Player player, InteractionHand hand) {
        particleCloud(level, player, player.getPosition(1));
        Vec3 teleportLocation = findTeleportLocation(level, player);
        particleCloud(level, player, teleportLocation);
    }

    private Vec3 findTeleportLocation(Level world, Player player) {
        //TODO: potentially cache this result
        var blockHitResult = Utils.getTargetBlock(world, player, ClipContext.Fluid.ANY, getSpellPower(player));
        var pos = blockHitResult.getBlockPos();

        //TODO: if this is a performance hit can just manually check the few blocks over this position
        int y = player.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());

        if (y - pos.getY() > 3 || blockHitResult.getType() == HitResult.Type.MISS) {
            return blockHitResult.getLocation();
        } else {
            return new Vec3(pos.getX(), y, pos.getZ());
        }
    }

    private void particleCloud(Level level, Player player, Vec3 pos) {
        if (level.isClientSide) {
            double width = 0.5;
            float height = player.getBbHeight() / 2;
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

    @Override
    public MutableComponent getUniqueInfo() {
        return Component.translatable("ui.testmod.distance", getSpellPower(null));
    }
}
