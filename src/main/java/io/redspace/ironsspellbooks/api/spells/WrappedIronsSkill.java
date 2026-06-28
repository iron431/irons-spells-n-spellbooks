//package io.redspace.ironsspellbooks.api.spells;
//
//import io.redspace.skillcasting.api.cast.CastContext;
//import io.redspace.skillcasting.api.skill.AbstractSkill;
//import io.redspace.skillcasting.api.skill.CastType;
//import net.minecraft.world.level.Level;
//import org.jetbrains.annotations.Nullable;
//
//public class WrappedIronsSkill extends AbstractSkill {
//
//    public @Nullable AbstractSpell shadowedSpell;
//    @Override
//    public CastType getCastType() {
//        return shadowedSpell == null ? CastType.INSTANT : shadowedSpell.getCastType();
//    }
//
//    @Override
//    public void onCast(Level level, CastContext castContext) {
//
//    }
//}