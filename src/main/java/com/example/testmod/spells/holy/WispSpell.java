package com.example.testmod.spells.holy;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.wisp.WispEntity;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class WispSpell extends AbstractSpell {
    public WispSpell() {
        this(1);
    }

    public WispSpell(int level) {
        super(SpellType.WISP_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 32;
        this.spellPowerPerLevel = 10;
        this.castTime = 40;
        this.baseManaCost = 30;
        this.cooldown = 40;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        int duration = (int) ((getSpellPower(entity) + baseSpellPower) * 100);
        TestMod.LOGGER.debug("WispSpell.onCast entityDuration:{}", duration);
        var wispEntity = new WispEntity(world, entity, getTargetLocation(world, entity), duration);
        var pos = Utils.getPositionFromEntityLookDirection(entity, 2);
        wispEntity.setPos(pos);
        var target = getTarget(world, entity);
        target.ifPresent(wispEntity::setTarget);
        world.addFreshEntity(wispEntity);
    }

    private Optional<LivingEntity> getTarget(Level level, LivingEntity entity) {
        var startPos = Utils.getPositionFromEntityLookDirection(entity, 1);
        var endPos = Utils.getPositionFromEntityLookDirection(entity, getSpellPower(entity));
        var bb = new AABB(startPos, endPos);
        return level.getEntities((Entity) null, bb, e -> {
            return ((e instanceof LivingEntity) && (e instanceof Enemy));
        }).stream().findFirst().map(entity1 -> (LivingEntity) entity1);
    }

    private Vec3 getTargetLocation(Level level, LivingEntity entity) {
        //TODO: potentially cache this result
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.ANY, getDistance(entity));
        var pos = blockHitResult.getBlockPos();

        //TODO: if this is a performance hit can just manually check the few blocks over this position
        int y = entity.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());

        if (y - pos.getY() > 3 || blockHitResult.getType() == HitResult.Type.MISS) {
            return blockHitResult.getLocation();
        } else {
            return new Vec3(pos.getX(), y + 1, pos.getZ());
        }
    }

    private float getDistance(Entity sourceEntity) {
        return getSpellPower(sourceEntity);
    }
}
