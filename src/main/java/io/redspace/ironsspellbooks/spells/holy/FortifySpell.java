package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.network.spell.ClientboundAborptionParticles;
import io.redspace.ironsspellbooks.network.spell.ClientboundFortifyAreaParticles;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public class FortifySpell extends AbstractSpell {
    public FortifySpell() {
        this(1);
    }

    public static final float radius = 7;

    public FortifySpell(int level) {
        super(SpellType.FORTIFY_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 2;
        this.castTime = 40;
        this.baseManaCost = 40;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.absorption", Utils.stringTruncation(getSpellPower(null), 0)));
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(radius, 1)));
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
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        level.getEntitiesOfClass(LivingEntity.class, new AABB(entity.position().subtract(radius, radius, radius), entity.position().add(radius, radius, radius))).forEach((target) -> {
            if (Utils.shouldHealEntity(entity, target) && entity.distanceTo(target) <= radius) {
                target.addEffect(new MobEffectInstance(MobEffectRegistry.FORTIFY.get(), 20 * 120, (int) getSpellPower(entity), false, false, true));
                Messages.sendToPlayersTrackingEntity(new ClientboundAborptionParticles(target.position()), entity, true);

            }
        });
        Messages.sendToPlayersTrackingEntity(new ClientboundFortifyAreaParticles(entity.position()), entity, true);

        super.onCast(level, entity, playerMagicData);
    }
}
