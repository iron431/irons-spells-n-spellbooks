package com.example.testmod.spells.ice;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.icicle.IcicleProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class IcicleSpell extends AbstractSpell {
    public IcicleSpell() {
        this(1);
    }

    public final MutableComponent uniqueText;

    public IcicleSpell(int level) {
        super(SpellType.ICICLE_SPELL);
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
        IcicleProjectile icicle = new IcicleProjectile(world, entity);
        icicle.setPos(entity.position().add(0, entity.getEyeHeight() - icicle.getBoundingBox().getYsize() * .5f, 0));
        icicle.shoot(entity.getLookAngle());
        icicle.setDamage(getSpellPower(entity));
        world.addFreshEntity(icicle);
    }

    @Override
    public MutableComponent getUniqueInfo() {
        return uniqueText;
    }
}
