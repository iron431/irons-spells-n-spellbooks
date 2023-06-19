package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.guiding_bolt.GuidingBoltProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class GuidingBoltSpell extends AbstractSpell {
    public GuidingBoltSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", "15s"));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.HOLY)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public GuidingBoltSpell(int level) {
        super(SpellType.GUIDING_BOLT_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 15;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.GUIDING_BOLT_CAST.get());
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        GuidingBoltProjectile guidingBolt = new GuidingBoltProjectile(world, entity);
        guidingBolt.setPos(entity.position().add(0, entity.getEyeHeight() - guidingBolt.getBoundingBox().getYsize() * .5f, 0));
        guidingBolt.shoot(entity.getLookAngle());
        guidingBolt.setDamage(getDamage(entity));
        world.addFreshEntity(guidingBolt);
        super.onCast(world, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return getSpellPower(entity) * .5f;
    }

}
