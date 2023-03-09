package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LobCreeperSpell extends AbstractSpell {
    public LobCreeperSpell() {
        this(1);
    }

    public LobCreeperSpell(int level) {
        super(SpellType.LOB_CREEPER_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(null), 1)));
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.CREEPER_HURT);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        float speed = (6 + this.level) * .1f;
        float damage = getDamage(entity);
        CreeperHeadProjectile head = new CreeperHeadProjectile(entity, level, speed, damage);
        Vec3 spawn = entity.getEyePosition().add(entity.getForward());
        head.moveTo(spawn.x, spawn.y - head.getBoundingBox().getYsize() / 2, spawn.z, entity.getYRot() + 180, entity.getXRot());
        level.addFreshEntity(head);
        super.onCast(level, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return this.getSpellPower(entity);
    }
}
