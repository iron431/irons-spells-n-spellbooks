package io.redspace.ironsspellbooks.spells;

import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TargetedTargetAreaCastData extends CastTargetingData {
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