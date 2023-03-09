package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.lightning_lance.LightningLanceProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LightningLanceSpell extends AbstractSpell {
    public LightningLanceSpell(int level) {
        super(SpellType.LIGHTNING_LANCE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 40;
        this.baseManaCost = 50;
        this.cooldown = 600;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(null), 1))) ;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.LIGHTNING_LANCE_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.LIGHTNING_WOOSH_01.get());
    }

    @Override
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        super.onServerPreCast(level, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        LightningLanceProjectile lance = new LightningLanceProjectile(level, entity);
        lance.setPos(entity.position().add(0, entity.getEyeHeight(), 0).add(entity.getForward()));
        lance.shoot(entity.getLookAngle());
        lance.setDamage(getSpellPower(entity));
        level.addFreshEntity(lance);
        super.onCast(level, entity, playerMagicData);
    }
}
