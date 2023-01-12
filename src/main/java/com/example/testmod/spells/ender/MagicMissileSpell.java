package com.example.testmod.spells.ender;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.magic_missile.MagicMissileProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class MagicMissileSpell extends AbstractSpell {
    public MagicMissileSpell() {
        this(1);
    }

    public final MutableComponent uniqueText;

    public MagicMissileSpell(int level) {
        super(SpellType.MAGIC_MISSILE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 5;
        this.cooldown = 0;
        uniqueText = Component.translatable("ui.testmod.damage", Utils.stringTruncation(getSpellPower(null), 1));
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        MagicMissileProjectile magicMissileProjectile = new MagicMissileProjectile(world, entity);
        magicMissileProjectile.setPos(entity.position().add(0, entity.getEyeHeight() - magicMissileProjectile.getBoundingBox().getYsize() * .5f, 0));
        magicMissileProjectile.shoot(entity.getLookAngle());
        magicMissileProjectile.setDamage(getSpellPower(entity));
        world.addFreshEntity(magicMissileProjectile);
    }

    @Override
    public MutableComponent getUniqueInfo() {
        return uniqueText;
    }
}
