package io.redspace.ironsspellbooks.spells.poison;

import com.mojang.datafixers.util.Either;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.poison_arrow.PoisonArrow;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;

import java.util.List;
import java.util.Optional;

public class PoisonArrowSpell extends AbstractSpell {

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getArrowDamage(caster), 1)),
                Component.translatable("ui.irons_spellbooks.aoe_damage", Utils.stringTruncation(getAOEDamage(caster), 1)));
    }

    public PoisonArrowSpell(int level) {
        super(SpellType.POISON_ARROW_SPELL);
        this.level = level;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
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
        PoisonArrow magicArrow = new PoisonArrow(level, entity);
        magicArrow.setPos(entity.position().add(0, entity.getEyeHeight() - magicArrow.getBoundingBox().getYsize() * .5f, 0).add(entity.getForward()));
        magicArrow.shoot(entity.getLookAngle());
        magicArrow.setDamage(getArrowDamage(entity));
        magicArrow.setAoeDamage(getAOEDamage(entity));
        level.addFreshEntity(magicArrow);
        super.onCast(level, entity, playerMagicData);
    }

    public float getArrowDamage(LivingEntity caster) {
        return getSpellPower(caster);
    }

    public float getAOEDamage(LivingEntity caster) {
        return getSpellPower(caster) * .185f;
    }

    public static ResourceLocation ANIMATION_CAST_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "charge_arrow");
    private final AnimationBuilder ANIMATION_CAST = new AnimationBuilder().addAnimation(ANIMATION_CAST_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    @Override
    public Either<AnimationBuilder, ResourceLocation> getCastStartAnimation(Player player) {
        return player == null ? Either.left(ANIMATION_CAST) : Either.right(ANIMATION_CAST_RESOURCE);
    }
}
