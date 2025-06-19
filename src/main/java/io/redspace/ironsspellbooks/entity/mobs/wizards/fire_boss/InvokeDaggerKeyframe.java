package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import net.minecraft.world.phys.Vec3;

public class InvokeDaggerKeyframe extends AttackKeyframe {
    public InvokeDaggerKeyframe(int timeStamp) {
        super(timeStamp, Vec3.ZERO);
    }
}
