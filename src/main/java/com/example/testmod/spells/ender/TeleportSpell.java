package com.example.testmod.spells.ender;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;

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
        var blockHitResult = Utils.getTargetBlock(world, player, ClipContext.Fluid.ANY, getSpellPower(player));
        var pos = blockHitResult.getBlockPos();

        //TODO: if this is a performance hit can just manually check the few blocks over this position
        int y = player.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());

        TestMod.LOGGER.debug("teleport: loc: {}", blockHitResult.getLocation());
        TestMod.LOGGER.debug("teleport: blockhit: {}, {}, {}", blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ());
        TestMod.LOGGER.debug("teleport: y: {}", y);

        if (y - pos.getY() > 3 || blockHitResult.getType() == HitResult.Type.MISS) {
            var dest = blockHitResult.getLocation();
            player.teleportTo((int) dest.x, (int) dest.y, (int) dest.z);
        } else {
            player.teleportTo(pos.getX(), y, pos.getZ());
        }
        player.resetFallDistance();
    }

    @Override
    public MutableComponent getUniqueInfo() {
        return Component.translatable("ui.testmod.distance", getSpellPower(null));
    }
}
