package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.goals;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.AnimatedActionGoal;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.entity.spells.wall_of_fire.WallOfFireEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FireWallAbilityGoal extends AnimatedActionGoal<FireBossEntity> {
    public static final int ANIM_DURATION = 10;
    public static final int ACTION_TIMESTAMP = 1;

    public FireWallAbilityGoal(FireBossEntity mob) {
        super(mob);
    }

    @Override
    protected boolean canStartAction() {
        return mob.getTarget() != null;
    }

    @Override
    protected int getActionTimestamp() {
        return ACTION_TIMESTAMP;
    }

    @Override
    protected int getActionDuration() {
        return ANIM_DURATION;
    }

    @Override
    protected int getCooldown() {
        return Utils.random.nextIntBetweenInclusive(40, 80);
    }

    @Override
    protected String getAnimationId() {
        return "instant_slash";
    }

    @Override
    protected void doAction() {
        var target = mob.getTarget();
        if (target != null) {
            mob.playSound(SoundRegistry.FIRE_CAST.get(), 2f, Utils.random.nextIntBetweenInclusive(80, 110) * .01f);

            Vec3 pos = target.position();
            float range = 5;
            Vec3 pointer = pos.subtract(mob.position()).normalize().scale(range);
            List<Vec3> anchors = new ArrayList<>();
            int count = 8;
            for (int i = 0; i < count; i++) {
                float rot = Mth.lerp(i / (float) count - 1, -Mth.PI, Mth.PI);
                anchors.add(Utils.moveToRelativeGroundLevel(mob.level, pos.add(pointer.yRot(rot)), 8));
            }
            WallOfFireEntity wallOfFire = new WallOfFireEntity(mob.level, mob, anchors, (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
            wallOfFire.setPos(pos);
            mob.level.addFreshEntity(wallOfFire);
        }
    }
}
