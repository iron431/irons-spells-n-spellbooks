package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import net.minecraft.world.entity.LivingEntity;

public class TargetedTargetAreaCastData extends TargetEntityCastData {
    final TargetAreaCastData targetAreaCastData;

    public TargetedTargetAreaCastData(LivingEntity target, TargetedAreaEntity targetedAreaEntity) {
        super(target);
        this.targetAreaCastData = new TargetAreaCastData(target.position(), targetedAreaEntity);
    }

    public TargetedAreaEntity getAreaEntity() {
        return targetAreaCastData.getCastingEntity();
    }

    @Override
    public void reset() {
        super.reset();
        targetAreaCastData.reset();
    }
}