package io.redspace.ironsspellbooks.spells.void_school;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.void_tentacle.VoidTentacle;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.BlockPos;
import io.redspace.ironsspellbooks.util.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class VoidTentaclesSpell extends AbstractSpell {
    public VoidTentaclesSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRings() * 1.3f, 1))
        );
    }

    public VoidTentaclesSpell(int level) {
        super(SpellType.VOID_TENTACLES_SPELL);
        this.level = level;
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 2;
        this.castTime = 20;
        this.baseManaCost = 150;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.VOID_TENTACLES_START.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.VOID_TENTACLES_FINISH.get());
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        int rings = getRings();
        int count = 2;
        Vec3 center = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, 48).getLocation();
        level.playSound(entity instanceof Player player ? player : null, center.x, center.y, center.z, SoundRegistry.VOID_TENTACLES_FINISH.get(), SoundSource.AMBIENT, 1, 1);

        for (int r = 0; r < rings; r++) {
            float tentacles = count + r * 2;
            for (int i = 0; i < tentacles; i++) {
                Vec3 random = new Vec3(Utils.getRandomScaled(1), Utils.getRandomScaled(1), Utils.getRandomScaled(1));
                Vec3 spawn = center.add(new Vec3(0, 0, 1.3 * (r + 1)).yRot(((6.281f / tentacles) * i))).add(random);

                spawn = new Vec3(spawn.x, Utils.findRelativeGroundLevel(level, spawn, 8), spawn.z);
                if (!level.getBlockState(new BlockPos(spawn).below()).isAir()) {
                    VoidTentacle tentacle = new VoidTentacle(level, entity, getDamage(entity));
                    tentacle.moveTo(spawn);
                    tentacle.setYRot(level.getRandom().nextInt(360));
                    level.addFreshEntity(tentacle);
                }
            }
        }
        //In order to trigger sculk sensors
        level.gameEvent(null, GameEvent.ENTITY_ROAR, center);
        super.onCast(level, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return getSpellPower(entity);
    }

    private int getRings() {
        return 1 + level;
    }
}
