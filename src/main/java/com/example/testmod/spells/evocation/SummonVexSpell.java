package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.mobs.SummonedVex;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SummonVexSpell extends AbstractSpell {
    public SummonVexSpell() {
        this(1);
    }

    public SummonVexSpell(int level) {
        super(SpellType.SUMMON_VEX_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 20;
        this.baseManaCost = 50;
        this.cooldown = 300;
    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0f, 1.0f);
    }

    @Override
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0f, 1.0f);
    }

    @Override
    public void onClientCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.0f);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.0f);
        for (int i = 0; i < this.level; i++) {
            SummonedVex vex = new SummonedVex(world, entity, 5 * 60 * 20);
            vex.setPos(entity.getEyePosition().add(new Vec3(1, 1, 1).yRot(i * 25)));
            vex.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(vex.getOnPos()), MobSpawnType.MOB_SUMMONED, null, null);
            world.addFreshEntity(vex);
        }
    }
}
