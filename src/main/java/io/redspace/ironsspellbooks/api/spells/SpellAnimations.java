package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.resources.ResourceLocation;

public class SpellAnimations {
    public static ResourceLocation ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animation");

    public static final AnimationHolder ANIMATION_INSTANT_CAST = new AnimationHolder(IronsSpellbooks.id("instant_projectile"), true);
    public static final AnimationHolder ANIMATION_CONTINUOUS_CAST = new AnimationHolder(IronsSpellbooks.id("continuous_thrust"), false);
    public static final AnimationHolder ANIMATION_CHARGED_CAST = new AnimationHolder(IronsSpellbooks.id("charged_throw"), true);
    public static final AnimationHolder ANIMATION_LONG_CAST = new AnimationHolder(IronsSpellbooks.id("long_cast"), true);
    public static final AnimationHolder ANIMATION_LONG_CAST_FINISH = new AnimationHolder(IronsSpellbooks.id("long_cast_finish"), true);
    public static final AnimationHolder ANIMATION_CONTINUOUS_OVERHEAD = new AnimationHolder(IronsSpellbooks.id("continuous_overhead"), false);
    public static final AnimationHolder SLASH_ANIMATION = new AnimationHolder(IronsSpellbooks.id("instant_slash"), true);
    public static final AnimationHolder BOW_CHARGE_ANIMATION = new AnimationHolder(IronsSpellbooks.id("charge_arrow"), true);
    public static final AnimationHolder CHARGE_WAVY_ANIMATION = new AnimationHolder(IronsSpellbooks.id("charge_wavy"), true);
    public static final AnimationHolder SELF_CAST_ANIMATION = new AnimationHolder(IronsSpellbooks.id("instant_self"), true);
    public static final AnimationHolder CHARGE_SPIT_ANIMATION = new AnimationHolder(IronsSpellbooks.id("charge_spit"), true);
    public static final AnimationHolder SPIT_FINISH_ANIMATION = new AnimationHolder(IronsSpellbooks.id("charge_spit_finish"), true);
    public static final AnimationHolder CHARGE_ANIMATION = new AnimationHolder(IronsSpellbooks.id("charge_black_hole"), true);
    public static final AnimationHolder FINISH_ANIMATION = new AnimationHolder(IronsSpellbooks.id("long_cast_finish"), true);
    public static final AnimationHolder OVERHEAD_MELEE_SWING_ANIMATION = new AnimationHolder(IronsSpellbooks.id("overhead_two_handed_swing"), true, true);
    public static final AnimationHolder TOUCH_GROUND_ANIMATION = new AnimationHolder(IronsSpellbooks.id("touch_ground"), true, true);
    public static final AnimationHolder ONE_HANDED_HORIZONTAL_SWING_ANIMATION = new AnimationHolder(IronsSpellbooks.id("horizontal_slash_one_handed"), true);
    public static final AnimationHolder ONE_HANDED_VERTICAL_UPSWING_ANIMATION = new AnimationHolder(IronsSpellbooks.id("katana_upslash"), true);
    public static final AnimationHolder CHARGE_RAISED_HAND = new AnimationHolder(IronsSpellbooks.id("charge_raised_hand"), false);
    public static final AnimationHolder STOMP = new AnimationHolder(IronsSpellbooks.id("stomp"), true, true);
    public static final AnimationHolder PREPARE_CROSS_ARMS = new AnimationHolder(IronsSpellbooks.id("cross_arms"), false);
    public static final AnimationHolder CAST_T_POSE = new AnimationHolder(IronsSpellbooks.id("cast_t_pose"), true);
    public static final AnimationHolder CAST_KNEELING_PRAYER = new AnimationHolder(IronsSpellbooks.id("kneeling_prayer"), false);
    public static final AnimationHolder SELF_CAST_TWO_HANDS = new AnimationHolder(IronsSpellbooks.id("self_cast_two_hands"), true);
    public static final AnimationHolder ANIMATION_CONTINUOUS_CAST_ONE_HANDED = new AnimationHolder(IronsSpellbooks.id("continuous_thrust_one_handed"), false);
    public static final AnimationHolder THROW_SINGLE_ITEM = new AnimationHolder(IronsSpellbooks.id("throw_item"), true);


}
