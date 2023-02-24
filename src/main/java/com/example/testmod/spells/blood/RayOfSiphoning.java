package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.damage.DamageSources;
import com.example.testmod.network.spell.ClientboundBloodSiphonParticles;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SchoolType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public class RayOfSiphoning extends AbstractSpell {
    public RayOfSiphoning() {
        this(1);
    }

    public RayOfSiphoning(int level) {
        super(SpellType.RAY_OF_SIPHONING_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 20;
        this.cooldown = 300;
        uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getTickDamage(null), 1)));
        uniqueInfo.add(Component.translatable("ui.testmod.distance", Utils.stringTruncation(getRange(), 1)));

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
        super.onCast(level, entity, playerMagicData);
        var hitResult = Utils.raycastForEntity(level, entity, getRange(), true);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            if (target instanceof LivingEntity) {
                DamageSources.applyDamage(target, getTickDamage(entity), getSpellType().getDamageSource(entity), SchoolType.BLOOD);
                entity.heal(getTickDamage(entity) * .35f);
                Messages.sendToPlayersTrackingEntity(new ClientboundBloodSiphonParticles(target.position().add(0, target.getBbHeight() / 2, 0), entity.position().add(0, entity.getBbHeight() / 2, 0)), entity, true);

            }
        }
    }

    private float getRange() {
        return 6 + this.getLevel() * 1.5f;
    }

    private float getTickDamage(Entity caster) {
        return getSpellPower(caster);
    }
}
