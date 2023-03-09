package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.wisp.WispEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 3;
        this.castTime = 40;
        this.baseManaCost = 30;
        this.cooldown = 40;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.MAGIC_SPELL_REVERSE_3.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.ARIAL_SUMMONING_5_CUSTOM_1.get());
    }

    public static SoundEvent getImpactSound() {
        return SoundRegistry.DARK_MAGIC_BUFF_03_CUSTOM_1.get();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        var wispEntity = new WispEntity(world, entity, getTargetLocation(world, entity), getSpellPower(entity));
        wispEntity.setPos(Utils.getPositionFromEntityLookDirection(entity, 2).subtract(0, .2, 0));
        wispEntity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_TIMER.get(), (int) getDuration(entity), 0, false, false, false));
        var target = getTarget(world, entity);

        IronsSpellbooks.LOGGER.debug("WispSpell.onCast entityDuration:{}, target:{}", getDuration(entity), target);

        target.ifPresent(wispEntity::setTarget);
        world.addFreshEntity(wispEntity);
        super.onCast(world, entity, playerMagicData);
    }

    private Optional<LivingEntity> getTarget(Level level, LivingEntity entity) {
        var startPos = Utils.getPositionFromEntityLookDirection(entity, 1);
        var endPos = Utils.getPositionFromEntityLookDirection(entity, getDistance(entity));
        var bb = new AABB(startPos, endPos);
        IronsSpellbooks.LOGGER.debug("WispSpell.getTarget: bb:{}", bb);
        return level.getEntities((Entity) null, bb, WispEntity::isValidTarget).stream().findFirst().map(e -> (LivingEntity) e);
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
        return getSpellPower(sourceEntity) * 5;
    }

    private float getDuration(Entity sourceEntity) {
        return ((getSpellPower(sourceEntity)) * 10);
    }
}
