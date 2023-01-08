package com.example.testmod.spells.fire;


import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.cone_of_cold.ConeOfColdProjectile;
import com.example.testmod.entity.fire_breath.FireBreathProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FireBreathSpell extends AbstractSpell {
    public FireBreathSpell() {
        this(1);
    }

    public FireBreathSpell(int level) {
        super(SpellType.FIRE_BREATH_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 5;
        this.cooldown = 100;
    }

    @Override
    public void onCastComplete(Level world, Player player, PlayerMagicData playerMagicData) {
        playerMagicData.discardCone();
    }

    @Override
    protected void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getID() && playerMagicData.cone != null) {
            playerMagicData.cone.setDealDamageActive();
        } else{
            FireBreathProjectile fireBreathProjectile = new FireBreathProjectile(world, player);
            fireBreathProjectile.setPos(player.position().add(0, player.getEyeHeight() * .7, 0));
            fireBreathProjectile.setDamage(getSpellPower(player));
            world.addFreshEntity(fireBreathProjectile);
            playerMagicData.discardCone();
            playerMagicData.cone = fireBreathProjectile;
        }
    }
}
