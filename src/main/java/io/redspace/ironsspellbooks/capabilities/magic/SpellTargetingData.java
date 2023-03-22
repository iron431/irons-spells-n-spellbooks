package io.redspace.ironsspellbooks.capabilities.magic;

import net.minecraft.world.entity.LivingEntity;

public class SpellTargetingData {
    //TODO: make me abstract
    public SpellTargetingData() {
        target = null;
    }

    public SpellTargetingData(LivingEntity target) {
        this.target = target;
    }

    public LivingEntity target;

    public boolean isTargeted(LivingEntity livingEntity){
        return livingEntity == target;
    }
}
