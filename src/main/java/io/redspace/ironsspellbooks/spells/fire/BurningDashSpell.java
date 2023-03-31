package io.redspace.ironsspellbooks.spells.fire;

import io.redspace.ironsspellbooks.capabilities.magic.CastData;
import io.redspace.ironsspellbooks.capabilities.magic.CastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;


public class BurningDashSpell extends AbstractSpell {
    //package net.minecraft.client.renderer.entity.layers;
    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)));
    }

    public BurningDashSpell() {
        this(1);
    }

    public BurningDashSpell(int level) {
        super(SpellType.BURNING_DASH_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    @Override
    public void onClientCast(Level level, LivingEntity entity, CastData castData) {
        if (castData instanceof BurningDashCastData bdcd) {
            entity.hasImpulse = bdcd.hasImpulse;
            entity.setDeltaMovement(entity.getDeltaMovement().add(bdcd.x, bdcd.y, bdcd.z));
        }

        super.onClientCast(level, entity, castData);
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
        entity.hasImpulse = true;
        float multiplier = (15 + getSpellPower(entity)) / 12f;
        var vec = entity.getLookAngle().multiply(3, 1, 3).normalize().add(0, .25, 0).scale(multiplier);
        playerMagicData.setAdditionalCastData(new BurningDashCastData((float) vec.x, (float) vec.y, (float) vec.z, true));
        entity.setDeltaMovement(entity.getDeltaMovement().add(vec));
        if (entity.isOnGround())
            entity.setPos(entity.position().add(0, 1.2, 0));
        startSpinAttack(entity, 5 + 2 * level);

        world.getEntities(entity, entity.getBoundingBox().inflate(4)).forEach((target) -> {
            if (target.distanceToSqr(entity) < 16) {
                if (DamageSources.applyDamage(target, getDamage(entity), SpellType.BURNING_DASH_SPELL.getDamageSource(entity), SchoolType.FIRE))
                    target.setSecondsOnFire(3);
            }
        });
        MagicManager.spawnParticles(world, ParticleHelper.FIRE, entity.getX(), entity.getY(), entity.getZ(), 75, 1, 0, 1, .08, false);

        playerMagicData.getSyncedData().setSpinAttackType(SpinAttackType.FIRE);
        super.onCast(world, entity, playerMagicData);
    }

    private float getDamage(LivingEntity caster) {
        return 5 + getSpellPower(caster) / 2;
    }

    private void startSpinAttack(LivingEntity entity, int durationInTicks) {
        if (entity instanceof Player player)
            player.startAutoSpinAttack(durationInTicks);
        else if (entity instanceof AbstractSpellCastingMob mob)
            mob.startAutoSpinAttack(durationInTicks);
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
