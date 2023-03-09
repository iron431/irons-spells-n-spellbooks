package io.redspace.ironsspellbooks.spells.ice;


import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.cone_of_cold.ConeOfColdProjectile;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.EntityCastData;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
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
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(null), 1)));

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
