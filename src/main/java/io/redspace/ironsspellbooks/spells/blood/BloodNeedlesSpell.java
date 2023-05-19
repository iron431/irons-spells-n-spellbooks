package io.redspace.ironsspellbooks.spells.blood;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashProjectile;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.AnimationHolder;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import oshi.software.os.mac.MacOSThread;
import software.bernie.geckolib3.core.builder.ILoopType;

import java.util.List;
import java.util.Optional;

public class BloodNeedlesSpell extends AbstractSpell {
    public BloodNeedlesSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", getCount()));

    }

    public BloodNeedlesSpell(int level) {
        super(SpellType.BlOOD_NEEDLES_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;


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
        int count = getCount();
        float damage = getDamage(entity);
        int degreesPerNeedle = 360 / count;
        var raycast = Utils.raycastForEntity(world, entity, 32, true);
        for (int i = 0; i < count; i++) {
            BloodNeedle needle = new BloodNeedle(world, entity);
            int rotation = degreesPerNeedle * i - (degreesPerNeedle / 2);
            needle.setDamage(damage);
            needle.setZRot(rotation);
            Vec3 spawn = entity.getEyePosition().add(new Vec3(0, 1.5, 0).zRot(rotation * Mth.DEG_TO_RAD).xRot(-entity.getXRot() * Mth.DEG_TO_RAD).yRot(-entity.getYRot() * Mth.DEG_TO_RAD));
            needle.moveTo(spawn);
            needle.shoot(raycast.getLocation().subtract(spawn).normalize());
            world.addFreshEntity(needle);
        }
        super.onCast(world, entity, playerMagicData);
    }

//    public static final AnimationHolder SLASH_ANIMATION = new AnimationHolder("instant_slash", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
//
//    @Override
//    public AnimationHolder getCastStartAnimation() {
//        return SLASH_ANIMATION;
//    }

    private int getCount() {
        return 5;
//        return this.getRarity().getValue() + 3;
    }

    private float getDamage(LivingEntity caster) {
        return getSpellPower(caster) * .25f;
    }
}
