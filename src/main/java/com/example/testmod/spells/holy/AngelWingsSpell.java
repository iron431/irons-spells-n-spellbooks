package com.example.testmod.spells.holy;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AngelWingsSpell extends AbstractSpell {
    public AngelWingsSpell() {
        this(1);
    }

    final float twoPi = 6.283f;
    public final MutableComponent uniqueText;

    public AngelWingsSpell(int level) {
        super(SpellType.HEAL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
        this.cooldown = 400;
        uniqueText = Component.translatable("ui.testmod.ange_wings", Utils.stringTruncation(getSpellPower(null), 1));
    }

    private int getEffectDuration(LivingEntity entity) {
        return (int) getSpellPower(entity)*50;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ANGEL_WINGS.get(), getEffectDuration(entity)), entity);
    }

    @Override
    public MutableComponent getUniqueInfo() {
        return uniqueText;
    }
}
