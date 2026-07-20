package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.world.entity.Mob;

public class SingleUseSpellHandler {

    protected final AbstractSpell singleUseSpell;
    protected int singleUseDelay;
    protected final int singleUseLevel;

    public SingleUseSpellHandler(AbstractSpell singleUseSpell, int singleUseDelay, int singleUseLevel) {
        this.singleUseSpell = singleUseSpell;
        this.singleUseDelay = singleUseDelay;
        this.singleUseLevel = singleUseLevel;
    }

    public void tick() {
        singleUseDelay--;
    }

    public boolean attemptCastSpell(Mob mob) {
        if (canUseSingleUse(mob) && singleUseSpell != null && singleUseDelay <= 0) {
            if (SkillcastingUtils.attemptInitiateMobCast(mob, SkillRegistry.holder(singleUseSpell), singleUseLevel, null)) {
                markSingleUseCooldown(mob, 5 * 60 * 20);
                return true;
            }
            singleUseDelay = 60;
        }
        return false;
    }

    public void markSingleUseCooldown(Mob mob, int delay) {
        mob.getPersistentData().putLong("irons_spellbooks:single_use_timestamp", mob.level.getGameTime() + delay);
    }

    public boolean canUseSingleUse(Mob mob) {
        if (!mob.getPersistentData().contains("irons_spellbooks:single_use_timestamp")) {
            return true;
        } else {
            long timestamp = mob.getPersistentData().getLong("irons_spellbooks:single_use_timestamp");
            if (mob.level().getGameTime() > timestamp) {
                mob.getPersistentData().remove("irons_spellbooks:single_use_timestamp");
                return true;
            } else {
                return false;
            }
        }
    }
}
