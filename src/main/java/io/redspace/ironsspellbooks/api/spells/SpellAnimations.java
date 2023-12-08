package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.builder.ILoopType;

public class SpellAnimations {
    public static ResourceLocation ANIMATION_RESOURCE = new ResourceLocation(IronsSpellbooks.MODID, "animation");

    public static final AnimationHolder ANIMATION_INSTANT_CAST = new AnimationHolder("instant_projectile", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_CONTINUOUS_CAST = new AnimationHolder("continuous_thrust", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    public static final AnimationHolder ANIMATION_CHARGED_CAST = new AnimationHolder("charged_throw", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_LONG_CAST = new AnimationHolder("long_cast", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_LONG_CAST_FINISH = new AnimationHolder("long_cast_finish", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder ANIMATION_CONTINUOUS_OVERHEAD = new AnimationHolder("continuous_overhead", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    public static final AnimationHolder SLASH_ANIMATION = new AnimationHolder("instant_slash", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder BOW_CHARGE_ANIMATION = new AnimationHolder("charge_arrow", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder CHARGE_WAVY_ANIMATION = new AnimationHolder("charge_wavy", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder SELF_CAST_ANIMATION = new AnimationHolder("instant_self", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder CHARGE_SPIT_ANIMATION = new AnimationHolder("charge_spit", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder SPIT_FINISH_ANIMATION = new AnimationHolder("charge_spit_finish", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder CHARGE_ANIMATION = new AnimationHolder("charge_black_hole", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder FINISH_ANIMATION = new AnimationHolder("long_cast_finish", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder OVERHEAD_MELEE_SWING_ANIMATION = new AnimationHolder("overhead_two_handed_swing", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    public static final AnimationHolder TOUCH_GROUND = new AnimationHolder("touch_ground", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
}
