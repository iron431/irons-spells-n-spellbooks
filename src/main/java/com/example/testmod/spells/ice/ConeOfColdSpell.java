package com.example.testmod.spells.ice;


import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.AbstractConeProjectile;
import com.example.testmod.entity.cone_of_cold.ConeOfColdProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.EntityCastData;
import com.example.testmod.spells.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ConeOfColdSpell extends AbstractSpell {
    public ConeOfColdSpell() {
        this(1);
    }

    public ConeOfColdSpell(int level) {
        super(SpellType.CONE_OF_COLD_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 5;
        this.cooldown = 100;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.isCasting()
                && playerMagicData.getCastingSpellId() == this.getID()
                && playerMagicData.getAdditionalCastData() instanceof EntityCastData entityCastData
                && entityCastData.getCastingEntity() instanceof AbstractConeProjectile cone) {
            cone.setDealDamageActive();
        } else {
            ConeOfColdProjectile coneOfColdProjectile = new ConeOfColdProjectile(world, entity);
            coneOfColdProjectile.setPos(entity.position().add(0, entity.getEyeHeight() * .7, 0));
            coneOfColdProjectile.setDamage(getSpellPower(entity));
            world.addFreshEntity(coneOfColdProjectile);
            playerMagicData.setAdditionalCastData(new EntityCastData(coneOfColdProjectile));
            super.onCast(world, entity, playerMagicData);
        }
    }
}
