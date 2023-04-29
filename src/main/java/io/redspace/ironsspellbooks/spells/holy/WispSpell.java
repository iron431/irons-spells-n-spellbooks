package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity;
import io.redspace.ironsspellbooks.network.spell.ClientboundSyncTargetingData;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WispSpell extends AbstractSpell {

    public WispSpell() {
        this(1);
    }

    public WispSpell(int level) {
        super(SpellType.WISP_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 15;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.MAGIC_SPELL_REVERSE_3.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.ARIAL_SUMMONING_5_CUSTOM_1.get());
    }

    public static SoundEvent getImpactSound() {
        return SoundRegistry.DARK_MAGIC_BUFF_03_CUSTOM_1.get();
    }

    @Override
    public boolean checkPreCastConditions(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        var target = findTarget(entity);
        if (target == null)
            return false;
        else {
            playerMagicData.setAdditionalCastData(new CastTargetingData(target));
            if (entity instanceof ServerPlayer serverPlayer)
                Messages.sendToPlayer(new ClientboundSyncTargetingData(target, getSpellType()), serverPlayer);
            return true;
        }
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof CastTargetingData targetingData) {
            var targetEntity = targetingData.getTarget((ServerLevel) world);
            WispEntity wispEntity = new WispEntity(world, entity, getSpellPower(entity));
            wispEntity.setTarget(targetEntity);
            wispEntity.setPos(Utils.getPositionFromEntityLookDirection(entity, 2).subtract(0, .2, 0));
            world.addFreshEntity(wispEntity);
            IronsSpellbooks.LOGGER.debug("WispSpell.onCast entityDuration:{}, target:{}", getDuration(entity), targetEntity);
        }

        //wispEntity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMON_TIMER.get(), (int) getDuration(entity), 0, false, false, false));


        super.onCast(world, entity, playerMagicData);
    }

    @Nullable
    private LivingEntity findTarget(LivingEntity caster) {
        var target = Utils.raycastForEntity(caster.level, caster, 32, true, 0.35f);
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            return livingTarget;
        } else {
            return null;
        }
    }

    private float getDistance(Entity sourceEntity) {
        return getSpellPower(sourceEntity) * 5;
    }

    private float getDuration(Entity sourceEntity) {
        return ((getSpellPower(sourceEntity)) * 10);
    }
}
