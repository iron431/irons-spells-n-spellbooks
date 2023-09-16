package io.redspace.ironsspellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractEldritchSpell extends AbstractSpell {

    @Override
    public boolean allowLooting() {
        return false;
    }

    @Override
    public boolean allowCrafting(Player player) {
        return false;
        //return isLearned(player);
    }

    @Override
    public boolean obfuscateStats(Player player) {
        return PlayerAdvancements;
        //return isLearned(player);
    }

    //public abstract boolean isLearned(Player player);
}
