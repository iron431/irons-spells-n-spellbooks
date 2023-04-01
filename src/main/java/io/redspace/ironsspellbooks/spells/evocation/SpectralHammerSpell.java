package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spectral_hammer.SpectralHammer;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SpectralHammerSpell extends AbstractSpell {

    public SpectralHammerSpell() {
        this(1);
    }

    public SpectralHammerSpell(int level) {
        super(SpellType.SPECTRAL_HAMMER_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 15;
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
        var blockPosition = Utils.getTargetBlock(world, entity, ClipContext.Fluid.ANY, getDistance(entity));
        Vec3 position;
        if (blockPosition.getType() == HitResult.Type.MISS) {
            position = Utils.getPositionFromEntityLookDirection(entity, getDistance(entity));
        } else {
            var pos = entity.getEyePosition();
            var distance = (float) Math.sqrt(blockPosition.getBlockPos().distToLowCornerSqr(pos.x, pos.y, pos.z));
            position = Utils.getPositionFromEntityLookDirection(entity, distance - 1.5f);
        }

        var spectralHammer = new SpectralHammer(world, entity, blockPosition, getSpellPower(entity));
        spectralHammer.setPos(position.x, position.y - 1, position.z);
        IronsSpellbooks.LOGGER.debug("SpectralHammerSpell.onCast pos:{}", position);
        world.addFreshEntity(spectralHammer);
        //super.onCast(world, entity, playerMagicData);
    }

    private float getDistance(Entity sourceEntity) {
        return getSpellPower(sourceEntity) * 2;
    }
}
