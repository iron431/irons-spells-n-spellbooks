package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.icicle.IcicleProjectile;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class IcicleSpell extends AbstractSpell {
    public IcicleSpell() {
        this(1);
    }

    public IcicleSpell(int level) {
        super(SpellType.ICICLE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 15;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(null), 1))) ;
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
        IcicleProjectile icicle = new IcicleProjectile(world, entity);
        icicle.setPos(entity.position().add(0, entity.getEyeHeight() - icicle.getBoundingBox().getYsize() * .5f, 0));
        icicle.shoot(entity.getLookAngle());
        icicle.setDamage(getSpellPower(entity));
        icicle.setNoGravity(true);
        world.addFreshEntity(icicle);
        super.onCast(world, entity, playerMagicData);
    }
}
