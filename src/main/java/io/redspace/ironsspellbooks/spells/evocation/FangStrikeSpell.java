package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.ExtendedEvokerFang;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FangStrikeSpell extends AbstractSpell {
    public FangStrikeSpell() {
        this(1);
    }

    public FangStrikeSpell(int level) {
        super(SpellType.FANG_STRIKE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 5;
        this.cooldown = 0;

        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.fang_count", getCount()));
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(null), 1)));
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        Vec3 forward = entity.getForward().multiply(1, 0, 1).normalize();
        Vec3 start = entity.getEyePosition().add(forward.scale(3));

        for (int i = 0; i < getCount(); i++) {
            Vec3 spawn = start.add(forward.scale(i));
            spawn = new Vec3(spawn.x, getGroundLevel(world, spawn, 8), spawn.z);
            if (!world.getBlockState(new BlockPos(spawn).below()).isAir()) {
                ExtendedEvokerFang fang = new ExtendedEvokerFang(world, spawn.x, spawn.y, spawn.z, (entity.getYRot() - 90) * Mth.DEG_TO_RAD, i, entity, getDamage(entity));
                world.addFreshEntity(fang);
            }

        }
        super.onCast(world, entity, playerMagicData);
    }

    private int getGroundLevel(Level level, Vec3 start, int maxSteps) {
        if (!level.getBlockState(new BlockPos(start)).isAir()) {
            for (int i = 0; i < maxSteps; i++) {
                start = start.add(0, 1, 0);
                if (level.getBlockState(new BlockPos(start)).isAir())
                    break;
            }
        }
        //Vec3 upper = level.clip(new ClipContext(start, start.add(0, maxSteps, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation();
        Vec3 lower = level.clip(new ClipContext(start, start.add(0, maxSteps * -2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation();
        return (int) lower.y;
    }

    private int getCount() {
        return 5 + level;
    }

    private float getDamage(LivingEntity entity) {
        return getSpellPower(entity) * .5f;
    }
}
