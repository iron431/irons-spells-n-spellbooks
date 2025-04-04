package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import net.minecraft.world.phys.Vec3;

public class JumpKeyframe extends AttackKeyframe {
    public JumpKeyframe(int timeStamp, Vec3 lungeVector) {
        super(timeStamp, lungeVector);
    }
}
