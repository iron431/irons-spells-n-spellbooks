package com.example.testmod.spells.lightning;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LightningBoltSpell extends AbstractSpell {
    public LightningBoltSpell() {
        this(1);
    }

    public LightningBoltSpell(int level) {
        super(SpellType.LIGHTNING_BOLT_SPELL);
        this.level = level;
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 4;
        this.castTime = 0;
        this.baseManaCost = 10;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getSpellPower(null), 1))) ;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        Vec3 pos = Utils.raycastForEntity(level, entity, 100, true).getLocation();
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        lightningBolt.setDamage(getSpellPower(entity));
        lightningBolt.setPos(pos);
        if (entity instanceof ServerPlayer serverPlayer)
            lightningBolt.setCause(serverPlayer);
        level.addFreshEntity(lightningBolt);
        super.onCast(level, entity, playerMagicData);
    }
}
