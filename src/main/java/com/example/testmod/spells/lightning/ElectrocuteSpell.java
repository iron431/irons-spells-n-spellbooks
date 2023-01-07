package com.example.testmod.spells.lightning;

import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.entity.electrocute.ElectrocuteProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public class ElectrocuteSpell extends AbstractSpell {
    public ElectrocuteSpell() {
        this(1);
    }

    public ElectrocuteSpell(int level) {
        super(SpellType.ELECTROCUTE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 10;
        this.cooldown = 100;
    }

    @Override
    protected void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getID() && playerMagicData.cone != null) {
            playerMagicData.cone.setDealDamageActive();
        } else{
            ElectrocuteProjectile electrocuteProjectile = new ElectrocuteProjectile(world, player);
            electrocuteProjectile.setPos(player.position().add(0, player.getEyeHeight() * .7, 0));
            electrocuteProjectile.setDamage(getSpellPower(player));
            world.addFreshEntity(electrocuteProjectile);
            playerMagicData.discardCone();
            playerMagicData.cone = electrocuteProjectile;
        }
    }
}
