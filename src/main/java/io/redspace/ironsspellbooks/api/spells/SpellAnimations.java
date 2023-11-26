package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.Animation;

public class SpellAnimations {
    public static ResourceLocation ANIMATION_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "animation");

    public static final AnimationHolder ANIMATION_INSTANT_CAST = new AnimationHolder("instant_projectile", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_CONTINUOUS_CAST = new AnimationHolder("continuous_thrust", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final AnimationHolder ANIMATION_CHARGED_CAST = new AnimationHolder("charged_throw", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_LONG_CAST = new AnimationHolder("long_cast", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_LONG_CAST_FINISH = new AnimationHolder("long_cast_finish", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_CONTINUOUS_OVERHEAD = new AnimationHolder("continuous_overhead", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final AnimationHolder SLASH_ANIMATION = new AnimationHolder("instant_slash", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder BOW_CHARGE_ANIMATION = new AnimationHolder("charge_arrow", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder CHARGE_WAVY_ANIMATION = new AnimationHolder("charge_wavy", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder SELF_CAST_ANIMATION = new AnimationHolder("instant_self", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder CHARGE_SPIT_ANIMATION = new AnimationHolder("charge_spit", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder SPIT_FINISH_ANIMATION = new AnimationHolder("charge_spit_finish", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder CHARGE_ANIMATION = new AnimationHolder("charge_black_hole", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder FINISH_ANIMATION = new AnimationHolder("long_cast_finish", Animation.LoopType.PLAY_ONCE);
    public static final AnimationHolder OVERHEAD_MELEE_SWING_ANIMATION = new AnimationHolder("long_cast_finish", IAnimation.LoopType.PLAY_ONCE);
}
