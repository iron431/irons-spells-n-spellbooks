package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.ExtendedWitherSkull;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class WitherSkullSpell extends AbstractSpell {
    public WitherSkullSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(caster), 1)));
    }

    public WitherSkullSpell(int level) {
        super(SpellType.WITHER_SKULL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 25;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.WITHER_SHOOT);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        float speed = (8 + this.level) * .01f;
        float damage = getDamage(entity);
        ExtendedWitherSkull skull = new ExtendedWitherSkull(entity, level, speed, damage);
        Vec3 spawn = entity.getEyePosition().add(entity.getForward());
        skull.moveTo(spawn.x, spawn.y - skull.getBoundingBox().getYsize() / 2, spawn.z, entity.getYRot() + 180, entity.getXRot());
        level.addFreshEntity(skull);
        super.onCast(level, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return this.getSpellPower(entity);
    }
}
