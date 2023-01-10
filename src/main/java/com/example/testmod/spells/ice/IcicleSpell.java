package com.example.testmod.spells.ice;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.firebolt.FireboltProjectile;
import com.example.testmod.entity.icicle.IcicleProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
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
    public void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        IcicleProjectile icicle = new IcicleProjectile(world, player);
        icicle.setPos(player.position().add(0, player.getEyeHeight() - icicle.getBoundingBox().getYsize() * .5f, 0));
        icicle.shoot(player.getLookAngle());
        icicle.setDamage(getSpellPower(player));
        world.addFreshEntity(icicle);
    }

    @Override
    public MutableComponent getUniqueInfo() {
        return uniqueText;
    }
}
