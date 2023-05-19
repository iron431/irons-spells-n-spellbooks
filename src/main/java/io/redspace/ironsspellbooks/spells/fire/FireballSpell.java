package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.ExtendedLargeFireball;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class FireballSpell extends AbstractSpell {
    public FireballSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", getRadius(caster))
        );
    }

    public FireballSpell(int level) {
        super(SpellType.FIREBALL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 60;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FIREBALL_START.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        float speed = 2.5f;
        Vec3 direction = entity.getLookAngle().scale(speed);
        Vec3 origin = entity.getEyePosition();
        ExtendedLargeFireball fireball = new ExtendedLargeFireball(world, entity, direction.x(), direction.y(), direction.z(), getRadius(entity));
        fireball.setDamage(getDamage(entity));
        fireball.setPos(origin.add(direction.scale(.25)));
        world.addFreshEntity(fireball);
        super.onCast(world, entity, playerMagicData);
    }

    public float getDamage(Entity caster) {
        return 10 * getSpellPower(caster);
    }

    public int getRadius(Entity caster) {
        return (int) getSpellPower(caster);
    }
}
