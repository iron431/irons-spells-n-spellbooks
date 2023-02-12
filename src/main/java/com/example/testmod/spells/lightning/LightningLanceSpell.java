package com.example.testmod.spells.lightning;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.lightning_lance.LightningLanceProjectile;
import com.example.testmod.registries.SoundRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LightningLanceSpell extends AbstractSpell {
    public LightningLanceSpell(int level) {
        super(SpellType.LIGHTNING_LANCE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 10;
        this.cooldown = 100;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.DARK_MAGIC_BUFF_03_CUSTOM_1.get());
    }

    @Override
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        super.onServerPreCast(level, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        LightningLanceProjectile lance = new LightningLanceProjectile(level, entity);
        lance.setPos(entity.position().add(0, entity.getEyeHeight() + lance.getBoundingBox().getYsize() * .5f, 0).add(entity.getForward()));
        lance.shoot(entity.getLookAngle());
        //lance.setDamage(getSpellPower(entity));
        level.addFreshEntity(lance);
        super.onCast(level, entity, playerMagicData);
    }
}
