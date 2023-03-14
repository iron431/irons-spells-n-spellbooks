package io.redspace.ironsspellbooks.spells.holy;

import com.mojang.datafixers.util.Either;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;

import java.util.Optional;

public class HealSpell extends AbstractSpell {
    public HealSpell() {
        this(1);
    }

    final float twoPi = 6.283f;

    public HealSpell(int level) {
        super(SpellType.HEAL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
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
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.heal(getSpellPower(entity));
        int count = 16;
        float radius = 1.25f;
        for (int i = 0; i < count; i++) {
            double x, z;
            double theta = Math.toRadians(360 / count) * i;
            x = Math.cos(theta) * radius;
            z = Math.sin(theta) * radius;
            MagicManager.spawnParticles(world, ParticleTypes.HEART, entity.position().x + x, entity.position().y, entity.position().z + z, 1, 0, 0, 0, 0.1, false);
        }
        super.onCast(world, entity, playerMagicData);
    }

    public static ResourceLocation ANIMATION_CAST_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "instant_self");
    private final AnimationBuilder ANIMATION_CAST = new AnimationBuilder().addAnimation(ANIMATION_CAST_RESOURCE.getPath(), ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    @Override
    public Either<AnimationBuilder, ResourceLocation> getCastStartAnimation(Player player) {
        return player == null ? Either.left(ANIMATION_CAST) : Either.right(ANIMATION_CAST_RESOURCE);
    }
}
