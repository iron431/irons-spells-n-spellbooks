package io.redspace.ironsspellbooks.spells.fire;


import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.ExtendedSmallFireball;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BlazeStormSpell extends AbstractSpell {
    public BlazeStormSpell() {
        this(1);
    }
    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)));
    }
    public BlazeStormSpell(int level) {
        super(SpellType.BLAZE_STORM_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 80 + 5 * level;
        this.baseManaCost = 5;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BLAZE_AMBIENT);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        super.onCast(world, entity, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
        if ((playerMagicData.getCastDurationRemaining() + 1) % 5 == 0)
            shootBlazeFireball(level, entity);
    }

    private float getDamage(LivingEntity caster) {
        return getSpellPower(caster) * .4f;
    }

    public void shootBlazeFireball(Level world, LivingEntity entity) {
        float speed = 0.45f;
        Vec3 origin = entity.getEyePosition().add(entity.getForward().normalize().scale(.2f));
        SmallFireball fireball = new ExtendedSmallFireball(entity, world, speed, getDamage(entity), .05f);
        fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
        world.playSound(null, origin.x, origin.y, origin.z, SoundEvents.BLAZE_SHOOT, SoundSource.AMBIENT, 1.0f, 1.0f);
        world.addFreshEntity(fireball);
    }

    @Override
    protected void playSound(Optional<SoundEvent> sound, Entity entity, boolean playDefaultSound) {
        super.playSound(sound, entity, false);
    }
}
