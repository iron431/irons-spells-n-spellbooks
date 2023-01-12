package com.example.testmod.spells.lightning;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.electrocute.ElectrocuteProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


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
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getID() && playerMagicData.cone != null) {
            playerMagicData.cone.setDealDamageActive();
        } else{
            ElectrocuteProjectile electrocuteProjectile = new ElectrocuteProjectile(world, entity);
            electrocuteProjectile.setPos(entity.position().add(0, entity.getEyeHeight() * .7, 0));
            electrocuteProjectile.setDamage(getSpellPower(entity));
            world.addFreshEntity(electrocuteProjectile);
            playerMagicData.discardCone();
            playerMagicData.cone = electrocuteProjectile;
        }
    }
}
