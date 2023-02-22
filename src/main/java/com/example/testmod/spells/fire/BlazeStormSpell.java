package com.example.testmod.spells.fire;


import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class BlazeStormSpell extends AbstractSpell {
    public BlazeStormSpell() {
        this(1);
    }

    public BlazeStormSpell(int level) {
        super(SpellType.BLAZE_STORM_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 1;
        this.castTime = 100 + 20 * level;
        this.baseManaCost = 5;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.testmod.damage", 5));

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BLAZE_SHOOT);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        float speed = 3.25f;
        Vec3 direction = entity.getLookAngle().scale(speed);
        Vec3 origin = entity.getEyePosition();
        SmallFireball fireball = new SmallFireball(world, entity, direction.x(), direction.y(), direction.z());
        fireball.setPos(origin);
        world.addFreshEntity(fireball);
        super.onCast(world, entity, playerMagicData);
    }
}
