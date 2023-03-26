package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import io.redspace.ironsspellbooks.capabilities.magic.CastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.player.ClientRenderCache;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;


public class BurningDashSpell extends AbstractSpell {
    //package net.minecraft.client.renderer.entity.layers;

    public BurningDashSpell() {
        this(1);
    }

    public BurningDashSpell(int level) {
        super(SpellType.BURNING_DASH_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    @Override
    public void onClientCastComplete(Level level, LivingEntity entity, CastData castData) {
        if (castData instanceof BurningDashCastData bdcd) {
            entity.hasImpulse = bdcd.hasImpulse;
            entity.setDeltaMovement(entity.getDeltaMovement().add(bdcd.x, bdcd.y, bdcd.z));
        }

        super.onClientCastComplete(level, entity, castData);
    }

    @Override
    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, PlayerMagicData playerMagicData) {
        ClientRenderCache.lastSpinAttack = ClientRenderCache.SpinAttackType.FIRE;
        super.onClientPreCast(level, entity, hand, playerMagicData);
    }

    @Override
    public CastDataSerializable getEmptyCastData() {
        return new BurningDashCastData();
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
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        IronsSpellbooks.LOGGER.debug("BurningDashSpell.onCast:");


        entity.hasImpulse = true;
        var vec = entity.getLookAngle().add(.5, .5, 0.5).normalize();
        playerMagicData.setAdditionalCastData(new BurningDashCastData((float) vec.x, (float) vec.y, (float) vec.z, true));
        entity.setDeltaMovement(entity.getDeltaMovement().add(vec));

        super.onCast(world, entity, playerMagicData);

    }

    public class BurningDashCastData implements CastDataSerializable {
        float x;
        float y;
        float z;
        boolean hasImpulse;

        public BurningDashCastData() {
        }

        public BurningDashCastData(float x, float y, float z, boolean hasImpulse) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasImpulse = hasImpulse;
        }

        @Override
        public void writeToStream(FriendlyByteBuf buffer) {
            buffer.writeFloat(x);
            buffer.writeFloat(y);
            buffer.writeFloat(z);
            buffer.writeBoolean(hasImpulse);
        }

        @Override
        public void readFromStream(FriendlyByteBuf buffer) {
            this.x = buffer.readFloat();
            this.y = buffer.readFloat();
            this.z = buffer.readFloat();
            this.hasImpulse = buffer.readBoolean();
        }

        @Override
        public void reset() {

        }
    }
}
