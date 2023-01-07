package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.blood_slash.BloodSlashProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
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
    public void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        BloodSlashProjectile bloodSlash = new BloodSlashProjectile(world, player);
        bloodSlash.setPos(player.getEyePosition());
        bloodSlash.shoot(player.getLookAngle());
        bloodSlash.setDamage(getSpellPower(player));
        world.addFreshEntity(bloodSlash);
    }
}
