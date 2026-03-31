package io.redspace.ironsspellbooks.entity.mobs.keeper.ability;

import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.MobAbilityInstance;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.MobAbilityType;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import net.minecraft.util.Mth;

// todo: make this class generic "melee ability instance"/"combat ability instance"? prob.
public class KeeperMeleeAbilityInstance extends MobAbilityInstance<KeeperEntity> {

    public KeeperMeleeAbilityInstance(MobAbilityType<KeeperEntity> type, KeeperEntity entity) {
        super(type, entity);
    }

    @Override
    public void tick() {
        forceFaceTarget();
        super.tick();
    }

    protected void forceFaceTarget() {
        var target = entity.getTarget();
        if (target == null) {
            return;
        }
        double d0 = target.getX() - entity.getX();
        double d1 = target.getZ() - entity.getZ();
        float yRot = (float) (Mth.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
        entity.setYBodyRot(yRot);
        entity.setYHeadRot(yRot);
        entity.setYRot(yRot);
    }
}
