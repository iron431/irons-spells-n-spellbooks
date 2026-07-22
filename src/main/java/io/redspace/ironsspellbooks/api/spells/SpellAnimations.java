package io.redspace.ironsspellbooks.api.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.resources.ResourceLocation;

public class SpellAnimations {
    public static ResourceLocation MOB_ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/casting_animations.json");

    private static AnimationHolder defaultFile(ResourceLocation resourceLocation) {
        return new AnimationHolder(MOB_ANIMATION_RESOURCE, resourceLocation);
    }

    private static AnimationHolder defaultFile(ResourceLocation resourceLocation, boolean animatesLegs) {
        return new AnimationHolder(MOB_ANIMATION_RESOURCE, resourceLocation, animatesLegs);
    }

    public static final AnimationHolder ANIMATION_INSTANT_CAST = defaultFile(IronsSpellbooks.id("instant_projectile"));
    public static final AnimationHolder ANIMATION_CONTINUOUS_CAST = defaultFile(IronsSpellbooks.id("continuous_thrust"));
    public static final AnimationHolder ANIMATION_CHARGED_CAST = defaultFile(IronsSpellbooks.id("charged_throw"));
    public static final AnimationHolder ANIMATION_LONG_CAST = defaultFile(IronsSpellbooks.id("long_cast"));
    public static final AnimationHolder ANIMATION_LONG_CAST_FINISH = defaultFile(IronsSpellbooks.id("long_cast_finish"));
    public static final AnimationHolder ANIMATION_CONTINUOUS_OVERHEAD = defaultFile(IronsSpellbooks.id("continuous_overhead"));
    public static final AnimationHolder SLASH_ANIMATION = defaultFile(IronsSpellbooks.id("instant_slash"));
    public static final AnimationHolder BOW_CHARGE_ANIMATION = defaultFile(IronsSpellbooks.id("charge_arrow"));
    public static final AnimationHolder CHARGE_WAVY_ANIMATION = defaultFile(IronsSpellbooks.id("charge_wavy"));
    public static final AnimationHolder SELF_CAST_ANIMATION = defaultFile(IronsSpellbooks.id("instant_self"));
    public static final AnimationHolder CHARGE_SPIT_ANIMATION = defaultFile(IronsSpellbooks.id("charge_spit"));
    public static final AnimationHolder SPIT_FINISH_ANIMATION = defaultFile(IronsSpellbooks.id("charge_spit_finish"));
    public static final AnimationHolder CHARGE_ANIMATION = defaultFile(IronsSpellbooks.id("charge_black_hole"));
    public static final AnimationHolder FINISH_ANIMATION = defaultFile(IronsSpellbooks.id("long_cast_finish"));
    public static final AnimationHolder OVERHEAD_MELEE_SWING_ANIMATION = defaultFile(IronsSpellbooks.id("overhead_two_handed_swing"), true);
    public static final AnimationHolder TOUCH_GROUND_ANIMATION = defaultFile(IronsSpellbooks.id("touch_ground"), true);
    public static final AnimationHolder ONE_HANDED_HORIZONTAL_SWING_ANIMATION = defaultFile(IronsSpellbooks.id("horizontal_slash_one_handed"));
    public static final AnimationHolder ONE_HANDED_VERTICAL_UPSWING_ANIMATION = defaultFile(IronsSpellbooks.id("katana_upslash"));
    public static final AnimationHolder CHARGE_RAISED_HAND = defaultFile(IronsSpellbooks.id("charge_raised_hand"));
    public static final AnimationHolder STOMP = defaultFile(IronsSpellbooks.id("stomp"), true);
    public static final AnimationHolder PREPARE_CROSS_ARMS = defaultFile(IronsSpellbooks.id("cross_arms"));
    public static final AnimationHolder CAST_T_POSE = defaultFile(IronsSpellbooks.id("cast_t_pose"));
    public static final AnimationHolder CAST_KNEELING_PRAYER = defaultFile(IronsSpellbooks.id("kneeling_prayer"));
    public static final AnimationHolder SELF_CAST_TWO_HANDS = defaultFile(IronsSpellbooks.id("self_cast_two_hands"));
    public static final AnimationHolder ANIMATION_CONTINUOUS_CAST_ONE_HANDED = defaultFile(IronsSpellbooks.id("continuous_thrust_one_handed"));
    public static final AnimationHolder THROW_SINGLE_ITEM = defaultFile(IronsSpellbooks.id("throw_item"));
    public static final AnimationHolder ONE_HANDED_RAY_CHARGE = defaultFile(IronsSpellbooks.id("charge_one_handed_ray"), true);
    public static final AnimationHolder ONE_HANDED_RAY_SHOOT = defaultFile(IronsSpellbooks.id("shoot_one_handed_ray"), true);
}
