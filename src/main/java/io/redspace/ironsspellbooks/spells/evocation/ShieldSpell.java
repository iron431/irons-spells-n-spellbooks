package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import io.redspace.ironsspellbooks.spells.*;
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

public class ShieldSpell extends AbstractSpell {

    public ShieldSpell() {
        this(1);
    }
    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(getShieldHP(caster), 1))
        );
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.EVOCATION)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public ShieldSpell(int level) {
        super(SpellType.SHIELD_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 10;
        this.baseManaCost = 35;
        this.castTime = 0;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_CAST_SPELL);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        ShieldEntity shield = new ShieldEntity(level, getShieldHP(entity));
        Vec3 spawn = Utils.raycastForEntity(level, entity, 5, true).getLocation();
        shield.setPos(spawn);
        shield.setRotation(entity.getXRot(), entity.getYRot());
        level.addFreshEntity(shield);
        super.onCast(level, entity, playerMagicData);
    }

    private float getShieldHP(LivingEntity caster) {
        return 10 + getSpellPower(caster);
    }

    //    @Override
//    public MutableComponent getUniqueInfo() {
//        return Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getDistance(null), 1));
//    }
}
