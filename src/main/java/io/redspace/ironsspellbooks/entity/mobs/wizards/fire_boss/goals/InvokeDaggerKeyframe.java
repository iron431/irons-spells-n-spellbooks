package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.goals;

import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import net.minecraft.world.phys.Vec3;

public class InvokeDaggerKeyframe extends AttackKeyframe {
    public final int duration;

    public InvokeDaggerKeyframe(int timeStamp, int duration) {
        super(timeStamp, Vec3.ZERO);
        this.duration = duration;
    }

    public InvokeDaggerKeyframe(int timeStamp) {
        this(timeStamp, 15);
    }
}
