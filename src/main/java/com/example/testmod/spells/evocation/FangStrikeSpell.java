package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.mobs.EnhancedEvokerFang;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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

        uniqueInfo.add(Component.translatable("ui.testmod.fang_count", getCount()));
        uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getDamage(null), 1)));
    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
    }

    @Override
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.0f, 1.0f);
    }

    @Override
    public void onClientCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        Vec3 forward = entity.getForward().multiply(1, 0, 1).normalize();
        Vec3 start = entity.getEyePosition().add(forward.scale(3));

        for (int i = 0; i < getCount(); i++) {
            Vec3 spawn = start.add(forward.scale(i));
            spawn = new Vec3(spawn.x, getGroundLevel(world, spawn, 8), spawn.z);
            if (!world.getBlockState(new BlockPos(spawn).below()).isAir()) {
                EnhancedEvokerFang fang = new EnhancedEvokerFang(world, spawn.x, spawn.y, spawn.z, (entity.getYRot() - 90) * Mth.DEG_TO_RAD, i, entity, getDamage(entity));
                world.addFreshEntity(fang);
            }

        }
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
