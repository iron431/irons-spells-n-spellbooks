package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashProjectile;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.AnimationHolder;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.builder.ILoopType;

import java.util.List;
import java.util.Optional;

public class BloodSlashSpell extends AbstractSpell {
    public BloodSlashSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(caster), 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.BLOOD)
            .setMaxLevel(5)
            .setCooldownSeconds(10)
            .build();

    public BloodSlashSpell(int level) {
        super(SpellType.BLOOD_SLASH_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;


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
    public void onCast(Level world, LivingEntity entity, MagicData playerMagicData) {
        BloodSlashProjectile bloodSlash = new BloodSlashProjectile(world, entity);
        bloodSlash.setPos(entity.getEyePosition());
        bloodSlash.shoot(entity.getLookAngle());
        bloodSlash.setDamage(getSpellPower(entity));
        world.addFreshEntity(bloodSlash);
        super.onCast(world, entity, playerMagicData);
    }

    public static final AnimationHolder SLASH_ANIMATION = new AnimationHolder("instant_slash", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SLASH_ANIMATION;
    }
}
