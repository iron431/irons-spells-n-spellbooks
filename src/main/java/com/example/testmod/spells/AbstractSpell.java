package com.example.testmod.spells;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.mana.client.ClientManaData;
import com.example.testmod.capabilities.mana.network.PacketCastSpell;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.fire.BurningDashSpell;
import com.example.testmod.spells.fire.FireballSpell;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class AbstractSpell {
    private final SpellType spellType;
    protected int level;
    protected int baseManaCost;
    protected int manaCostPerLevel;
    protected int baseSpellPower;
    protected int spellPowerPerLevel;
    protected int cooldown;
    protected int cooldownRemaining;

    public AbstractSpell(SpellType spellEnum) {
        this.spellType = spellEnum;
    }

    public int getID() {
        return this.spellType.getValue();
    }

    public int getLevel() {
        return this.level;
    }

    public int getManaCost() {
        return baseManaCost + manaCostPerLevel * (level - 1);
    }

    public int getSpellPower() {
        return baseSpellPower + spellPowerPerLevel * (level - 1);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public static AbstractSpell getSpell(SpellType spellType, int level) {
        switch (spellType) {
            case BURNING_DASH_SPELL -> { return new BurningDashSpell(level); }
            case FIREBALL_SPELL -> { return new FireballSpell(level); }
            default -> { return new FireballSpell(level); }
        }
    }

    public static AbstractSpell getSpell(int spellId, int level) {
        return getSpell(SpellType.values()[spellId], level);
    }

    //returns true/false for success/failure to cast
    public boolean attemptCast(ItemStack stack, Level world, Player player) {
        //fill with all casting criteria
        boolean canCast = !isOnCooldown() &&
                ClientManaData.getPlayerMana() >= getManaCost();

        if (canCast) {
            this.onCast(stack, world, player);
            if (!world.isClientSide()) {
                startCooldown(player);
                Messages.sendToServer(new PacketCastSpell(this));
            }
            return true;
        } else {
            return false;
        }
    }

    public void tick() {
        if (isOnCooldown()){
            TestMod.LOGGER.info(cooldownRemaining + "/" + cooldown + " ("+getPercentCooldown()*100+"%)");
            cooldownRemaining--;
        }
    }

    public abstract void onCast(ItemStack stack, Level world, Player player);

    public boolean isOnCooldown() {
        return cooldownRemaining > 0;
    }

    public int getModifiedCastCooldown(Player caster) {
        float attributeCooldownReductionPlaceholder = 1;
        return (int) (cooldown / attributeCooldownReductionPlaceholder);
    }

    public float getPercentCooldown() {
        //return 0.75f;
        return Mth.clamp(cooldownRemaining / ((float) cooldown), 0, 1);
    }

    public void startCooldown(@Nullable Player caster) {
        if (caster == null)
            cooldownRemaining = cooldown;
        else
            cooldownRemaining = getModifiedCastCooldown(caster);
    }

    @Override
    public boolean equals(Object obj) {
        AbstractSpell o = (AbstractSpell) obj;
        if (this.spellType == o.spellType && this.level == o.level) {
            return true;
        }
        return false;
    }
}
