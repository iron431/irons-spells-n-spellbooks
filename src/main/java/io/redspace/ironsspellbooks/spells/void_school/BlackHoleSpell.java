package io.redspace.ironsspellbooks.spells.void_school;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class BlackHoleSpell extends AbstractSpell {
    public BlackHoleSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(caster), 1))
        );
    }

    public BlackHoleSpell(int level) {
        super(SpellType.BLACK_HOLE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 100;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 2;
        this.castTime = 20;
        this.baseManaCost = 300;
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
        Vec3 center = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, 48).getLocation();
        level.playSound(entity instanceof Player player ? player : null, center.x, center.y, center.z, SoundRegistry.VOID_TENTACLES_FINISH.get(), SoundSource.AMBIENT, 1, 1);

        BlackHole blackHole = new BlackHole(level, entity);
        float radius = getRadius(entity);
        blackHole.setRadius(radius);
        blackHole.moveTo(center);
        level.addFreshEntity(blackHole);
        super.onCast(level, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return getSpellPower(entity);
    }

    private int getRadius(LivingEntity entity) {
        return 2 * level + 4;
    }
}
