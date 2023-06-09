package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WispSpell extends AbstractSpell {

    public WispSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(caster), 1)));
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.HOLY)
            .setMaxLevel(10)
            .setCooldownSeconds(3)
            .build();

    public WispSpell(int level) {
        super(SpellType.WISP_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 15;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_MIRROR);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.EVOCATION_CAST.get());
    }

    public static SoundEvent getImpactSound() {
        return SoundRegistry.DARK_MAGIC_BUFF_03_CUSTOM_1.get();
    }

    @Override
    public boolean checkPreCastConditions(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, getSpellType(), 48, .35f);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof CastTargetingData targetingData) {
            var targetEntity = targetingData.getTarget((ServerLevel) world);
            WispEntity wispEntity = new WispEntity(world, entity, getSpellPower(entity));
            wispEntity.setTarget(targetEntity);
            wispEntity.setPos(Utils.getPositionFromEntityLookDirection(entity, 2).subtract(0, .2, 0));
            world.addFreshEntity(wispEntity);
        }

        //wispEntity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_TIMER.get(), (int) getDuration(entity), 0, false, false, false));


        super.onCast(world, entity, playerMagicData);
    }

    @Nullable
    private LivingEntity findTarget(LivingEntity caster) {
        var target = Utils.raycastForEntity(caster.level(), caster, 64, true, 0.35f);
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            return livingTarget;
        } else {
            return null;
        }
    }

    private float getDistance(LivingEntity sourceEntity) {
        return getSpellPower(sourceEntity) * 5;
    }

    private float getDuration(LivingEntity sourceEntity) {
        return ((getSpellPower(sourceEntity)) * 10);
    }
}
