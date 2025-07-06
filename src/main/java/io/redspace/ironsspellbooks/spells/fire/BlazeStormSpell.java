package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpellSkill;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
import io.redspace.skillcastingapi.core.CastType;
import io.redspace.skillcastingapi.data.ICastContext;
import io.redspace.skillcastingapi.data.context_parameter.ContextParameterMap;
import io.redspace.skillcastingapi.data.context_parameter.DefaultContextParameters;
import io.redspace.skillcastingapi.skillcasting_events.BuildCastTimeEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class BlazeStormSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(ICastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(castContext), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public BlazeStormSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 60 - 5;
        this.baseManaCost = 5;
    }

    @Override
    public void buildContextParameters(ICastContext castContext, ContextParameterMap params) {
        int castTime = this.castTime + 5 * castContext.getSpellLevel();
        //todo: is this good practice?
        params.set(DefaultContextParameters.CAST_TIME, NeoForge.EVENT_BUS.post(new BuildCastTimeEvent(castContext, castTime)).getValue());
        super.buildContextParameters(castContext, params);

    }

    @Override
    public io.redspace.skillcastingapi.core.CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BLAZE_AMBIENT);
    }

    @Override
    public Optional<SoundEvent> getOnCastSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(ICastContext castContext) {
    }

    @Override
    public void onServerCastTick(ICastContext castContext) {
        if ((castContext.getSkillcastingData().castDurationRemaining(castContext.getLevel().getGameTime()) + 1) % 5 == 0) {
            shootBlazeFireball(castContext);
        }
    }

    private float getDamage(ICastContext castContext) {
        return getSpellPower(castContext) * .4f;
    }

    public void shootBlazeFireball(ICastContext castContext) {
        Vec3 origin = castContext.getPosition().add(castContext.getForward().normalize().scale(.2f));
        SmallMagicFireball fireball = new SmallMagicFireball(castContext.getLevel(), castContext.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null);
        fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
        fireball.shoot(castContext.getForward(), .05f);
        fireball.setDamage(getDamage(castContext));
        castContext.getLevel().playSound(null, origin.x, origin.y, origin.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 2.0f, 1.0f);
        castContext.getLevel().addFreshEntity(fireball);
    }

    //fixme: fire damage sources
//    @Override
//    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
//        return super.getDamageSource(projectile, attacker).setFireTicks(40).setIFrames(0);
//    }
}
