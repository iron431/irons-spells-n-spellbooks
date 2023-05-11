package io.redspace.ironsspellbooks.spells.poison;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrb;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class AcidOrbSpell extends AbstractSpell {

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(caster), 1)),
                Component.translatable("ui.irons_spellbooks.rend", Utils.stringTruncation((getRendAmplifier(caster) + 1) * 5, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getRendDuration(caster), 1)));
    }

    public AcidOrbSpell(int level) {
        super(SpellType.ACID_ORB_SPELL);
        this.level = level;
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 15;
        this.baseManaCost = 30;

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
        AcidOrb orb = new AcidOrb(level, entity);
        orb.setPos(entity.position().add(0, entity.getEyeHeight() - orb.getBoundingBox().getYsize() * .5f, 0).add(entity.getForward()));
        orb.shoot(entity.getLookAngle());
        orb.setExplosionRadius(getRadius(entity));
        orb.setRendLevel(getRendAmplifier(entity));
        orb.setRendDuration(getRendDuration(entity));
        level.addFreshEntity(orb);
        super.onCast(level, entity, playerMagicData);
    }

    public float getRadius(LivingEntity caster) {
        return getSpellPower(caster) * 3;
    }

    public int getRendAmplifier(LivingEntity caster) {
        return (int) (getSpellPower(caster) * this.level - 1);
    }

    public int getRendDuration(LivingEntity caster) {
        return (int) (getSpellPower(caster) * 20 * 15);
    }

    //public static ResourceLocation ANIMATION_CAST_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "charge_arrow");
    //private final AnimationBuilder ANIMATION_CAST = new AnimationBuilder().addAnimation(ANIMATION_CAST_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.PLAY_ONCE);
//
//    @Override
//    public Either<AnimationBuilder, ResourceLocation> getCastStartAnimation(Player player) {
//        return player == null ? Either.left(ANIMATION_CAST) : Either.right(ANIMATION_CAST_RESOURCE);
//    }
}
