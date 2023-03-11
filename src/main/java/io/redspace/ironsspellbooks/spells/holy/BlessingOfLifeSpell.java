package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SpellTargetingData;
import io.redspace.ironsspellbooks.network.spell.ClientboundHealParticles;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class BlessingOfLifeSpell extends AbstractSpell {
    public BlessingOfLifeSpell() {
        this(1);
    }

    public BlessingOfLifeSpell(int level) {
        super(SpellType.BLESSING_OF_LIFE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 10;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.healing", Utils.stringTruncation(getSpellPower(null), 1)));

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
    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        var target = getTarget(entity);
        if (target != null) {
            playerMagicData.setAdditionalCastData(new HealTargetingData(target));
        } else if (entity instanceof ServerPlayer sp) {
            //Utils.serverSideCancelCast(sp);
        }
        super.onServerPreCast(level, entity, playerMagicData);
    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
        var target = getTarget(entity);
        if (target != null) {
            var targetData = new SpellTargetingData();
            targetData.target = target;
            ClientMagicData.setTargetingData(targetData);
        }

        super.onClientPreCast(level, entity, hand, playerMagicData);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof HealTargetingData healTargetingData) {
            healTargetingData.targetEntity.heal(getSpellPower(entity));
            Messages.sendToPlayersTrackingEntity(new ClientboundHealParticles(healTargetingData.targetEntity.position()), healTargetingData.targetEntity, true);
        }

        super.onCast(world, entity, playerMagicData);
    }

    @Nullable
    private LivingEntity getTarget(LivingEntity caster) {
        var target = Utils.raycastForEntity(caster.level, caster, 32, true);
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            return livingTarget;
        } else {
            return null;
        }
    }

    public class HealTargetingData implements CastData {
        //private Entity castingEntity;
        private LivingEntity targetEntity;


        HealTargetingData(LivingEntity target) {
            this.targetEntity = target;
        }

        @Override
        public void reset() {

        }
    }
}
