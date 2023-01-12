package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.blood_slash.BloodSlashProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BloodSlashSpell extends AbstractSpell {
    public BloodSlashSpell() {
        this(1);
    }

    public BloodSlashSpell(int level) {
        super(SpellType.BLOOD_SLASH_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
        this.cooldown = 100;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        BloodSlashProjectile bloodSlash = new BloodSlashProjectile(world, entity);
        bloodSlash.setPos(entity.getEyePosition());
        bloodSlash.shoot(entity.getLookAngle());
        bloodSlash.setDamage(getSpellPower(entity));
        world.addFreshEntity(bloodSlash);
    }
}
