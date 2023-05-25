package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammer;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class SpectralHammerSpell extends AbstractSpell {

    public SpectralHammerSpell() {
        this(1);
    }

    private static final int distance = 12;

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.dimensions", 1 + getRadius(caster) * 2, 1 + getRadius(caster) * 2, getDepth(caster) + 1),
                Component.translatable("ui.irons_spellbooks.distance", distance)
        );
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchool(SchoolType.EVOCATION)
            .setMaxLevel(5)
            .setCooldownSeconds(10)
            .build();

    public SpectralHammerSpell(int level) {
        super(SpellType.SPECTRAL_HAMMER_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 25;
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
    public boolean checkPreCastConditions(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        return Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, distance).getType() == HitResult.Type.BLOCK;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        var blockPosition = Utils.getTargetBlock(world, entity, ClipContext.Fluid.NONE, distance);
        var face = blockPosition.getDirection();

        int radius = getRadius(entity);
        int depth = getDepth(entity);

        var spectralHammer = new SpectralHammer(world, entity, blockPosition, depth, radius);
        Vec3 position = Vec3.atCenterOf(blockPosition.getBlockPos());

        if (!face.getAxis().isVertical()) {
            position = position.subtract(0, 2, 0).subtract(entity.getForward().normalize().scale(1.5));
        }else if(face == Direction.DOWN){
            position = position.subtract(0, 3, 0);
        }

        spectralHammer.setPos(position.x, position.y, position.z);
        world.addFreshEntity(spectralHammer);
        //IronsSpellbooks.LOGGER.debug("SpectralHammerSpell.onCast pos:{}", position);
        super.onCast(world, entity, playerMagicData);
    }

    private int getDepth(LivingEntity caster) {
        return (int) getSpellPower(caster);
    }

    private int getRadius(LivingEntity caster) {
        return (int) Math.max(getSpellPower(caster) * .5f, 1);
    }
}
