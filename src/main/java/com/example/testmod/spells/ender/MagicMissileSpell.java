package com.example.testmod.spells.ender;

import com.example.testmod.entity.blood_slash.BloodSlashProjectile;
import com.example.testmod.entity.magic_missile.MagicMissileProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MagicMissileSpell extends AbstractSpell {
    public MagicMissileSpell() {
        this(1);
    }

    public final TranslatableComponent uniqueText;

    public MagicMissileSpell(int level) {
        super(SpellType.MAGIC_MISSILE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 5;
        this.cooldown = 0;
        uniqueText = new TranslatableComponent("ui.testmod.damage", Utils.stringTruncation(getDamage(), 1));
    }

    @Override
    public void onCast(Level world, Player player) {
        MagicMissileProjectile magicMissileProjectile = new MagicMissileProjectile(world, player);
        magicMissileProjectile.setPos(player.position().add(0, player.getEyeHeight(), 0));
        magicMissileProjectile.shoot(player.getLookAngle());
        magicMissileProjectile.setDamage(getDamage());
        world.addFreshEntity(magicMissileProjectile);
    }

    private float getDamage() {
        return getSpellPower();
    }

    @Override
    public TranslatableComponent getUniqueInfo() {
        return uniqueText;
    }
}
