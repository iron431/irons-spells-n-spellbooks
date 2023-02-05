package com.example.testmod.spells.holy;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.wisp.WispEntity;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.registries.SoundRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WispSpell extends AbstractSpell {
    public static DamageSource WISP_DAMAGE = new DamageSource("wisp_damage");

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
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        entity.playSound(SoundRegistry.MAGIC_SPELL_REVERSE_3.get(), 1.0f, 1.0f);
    }

    @Override
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        entity.playSound(SoundRegistry.MAGIC_SPELL_REVERSE_3.get(), 1.0f, 1.0f);
    }

    @Override
    public void onClientCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.playSound(SoundRegistry.ARIAL_SUMMONING_5_CUSTOM_1.get(), 1.0f, 1.0f);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.playSound(SoundRegistry.ARIAL_SUMMONING_5_CUSTOM_1.get(), 1.0f, 1.0f);
        var wispEntity = new WispEntity(world, entity, getTargetLocation(world, entity), getSpellPower(entity));
        wispEntity.setPos(Utils.getPositionFromEntityLookDirection(entity, 2).subtract(0, .2, 0));
        wispEntity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_TIMER.get(), (int) getDuration(entity), 0, false, false, false));
        var target = getTarget(world, entity);

        TestMod.LOGGER.debug("WispSpell.onCast entityDuration:{}, target:{}", getDuration(entity), target);

        target.ifPresent(wispEntity::setTarget);
        world.addFreshEntity(wispEntity);
    }

    private Optional<LivingEntity> getTarget(Level level, LivingEntity entity) {
        var startPos = Utils.getPositionFromEntityLookDirection(entity, 1);
        var endPos = Utils.getPositionFromEntityLookDirection(entity, getDistance(entity));
        var bb = new AABB(startPos, endPos);
        TestMod.LOGGER.debug("WispSpell.getTarget: bb:{}", bb);
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
