package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
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

public class MagicArrowSpell extends AbstractSpell {

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(caster), 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.ENDER)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public MagicArrowSpell(int level) {
        super(SpellType.MAGIC_ARROW_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 2;
        this.castTime = 30;
        this.baseManaCost = 40;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.MAGIC_ARROW_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.MAGIC_ARROW_RELEASE.get());
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        MagicArrowProjectile magicArrow = new MagicArrowProjectile(level, entity);
        magicArrow.setPos(entity.position().add(0, entity.getEyeHeight() - magicArrow.getBoundingBox().getYsize() * .5f, 0).add(entity.getForward()));
        magicArrow.shoot(entity.getLookAngle());
        magicArrow.setDamage(getSpellPower(entity));
        level.addFreshEntity(magicArrow);
        super.onCast(level, entity, playerMagicData);
    }

    public static final AnimationHolder BOW_CHARGE_ANIMATION = new AnimationHolder("charge_arrow", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    @Override
    public AnimationHolder getCastStartAnimation() {
        return BOW_CHARGE_ANIMATION;
    }


}
