package com.example.testmod.spells.evocation;

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

public class ShieldSpell extends AbstractSpell {

    public ShieldSpell() {
        this(1);
    }

    public ShieldSpell(int level) {
        super(SpellType.SHIELD_SPELL);
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

    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);

    }

//    @Override
//    public MutableComponent getUniqueInfo() {
//        return Component.translatable("ui.testmod.distance", Utils.stringTruncation(getDistance(null), 1));
//    }
}
