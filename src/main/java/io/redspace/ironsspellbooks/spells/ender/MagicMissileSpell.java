package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.magic_missile.MagicMissileProjectile;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class MagicMissileSpell extends AbstractSpell {
    public MagicMissileSpell() {
        this(1);
    }

    public MagicMissileSpell(int level) {
        super(SpellType.MAGIC_MISSILE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 15;
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
        MagicMissileProjectile magicMissileProjectile = new MagicMissileProjectile(world, entity);
        magicMissileProjectile.setPos(entity.position().add(0, entity.getEyeHeight() - magicMissileProjectile.getBoundingBox().getYsize() * .5f, 0));
        magicMissileProjectile.shoot(entity.getLookAngle());
        magicMissileProjectile.setDamage(getSpellPower(entity));
        world.addFreshEntity(magicMissileProjectile);
        super.onCast(world, entity, playerMagicData);
    }
}
