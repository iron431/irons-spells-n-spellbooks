package com.example.testmod.spells.fire;


import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.ExtendedSmallFireball;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BlazeStormSpell extends AbstractSpell {
    public BlazeStormSpell() {
        this(1);
    }

    public BlazeStormSpell(int level) {
        super(SpellType.BLAZE_STORM_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 80 + 10 * level;
        this.baseManaCost = 5;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getDamage(null), 1)));

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BLAZE_AMBIENT);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        super.onCast(world, entity, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        if ((playerMagicData.getCastDurationRemaining()) % 5 == 0)
            shootBlazeFireball(level, entity);
    }

    private float getDamage(LivingEntity caster) {
        return getSpellPower(caster) * .5f;
    }

    public void shootBlazeFireball(Level world, LivingEntity entity) {
        float speed = 0.45f;
        Vec3 origin = entity.getEyePosition();
        SmallFireball fireball = new ExtendedSmallFireball(entity, world, speed, getDamage(entity),.05f);
        fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
        world.playSound(null, origin.x, origin.y, origin.z, SoundEvents.BLAZE_SHOOT, SoundSource.AMBIENT, 1.0f, 1.0f);
        world.addFreshEntity(fireball);
    }
}
