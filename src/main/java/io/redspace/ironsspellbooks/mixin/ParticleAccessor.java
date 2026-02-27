package io.redspace.ironsspellbooks.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
    @Accessor("xo")
    void irons_spellbooks$xo(double d);
    @Accessor("yo")
    void irons_spellbooks$yo(double d);
    @Accessor("zo")
    void irons_spellbooks$zo(double d);
    @Accessor("gravity")
    void irons_spellbooks$gravity(float f);
    @Accessor("stoppedByCollision")
    void irons_spellbooks$stoppedByCollision(boolean b);
//    @Accessor("stoppedByCollision")
//    boolean irons_spellbooks$isStoppedByCollision();
//    @Accessor("xd")
//    void irons_spellbooks$xd(double d);
//    @Accessor("yd")
//    void irons_spellbooks$yd(double d);
//    @Accessor("zd")
//    void irons_spellbooks$zd(double d);
}
