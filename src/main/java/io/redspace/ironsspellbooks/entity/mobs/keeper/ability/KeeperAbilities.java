package io.redspace.ironsspellbooks.entity.mobs.keeper.ability;

import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.EventKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.EventKeyframeHandler;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.keyframes.SimpleMeleeKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.goals.abilities.keyframes.SoundEventKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class KeeperAbilities {

    public static final KeeperAbilityType DOUBLE_SLASH = new KeeperAbilityType(
            null,
            EventKeyframeHandler.of(
                    swingSound(9), hitFrame(13, false),
                    swingSound(25), hitFrame(29, true)
            ),
            43, "sword_double_slash",
            KeeperMeleeAbilityInstance::new, 1f
    );

    public static final KeeperAbilityType SINGLE_UPWARD = new KeeperAbilityType(
            null,
            EventKeyframeHandler.of(swingSound(9), hitFrame(13, true)),
            26, "sword_single_upward",
            KeeperMeleeAbilityInstance::new, 1f
    );

    public static final KeeperAbilityType SINGLE_HORIZONTAL = new KeeperAbilityType(
            null,
            EventKeyframeHandler.of(swingSound(8), hitFrame(12, true)),
            28, "sword_single_horizontal",
            KeeperMeleeAbilityInstance::new, 1f
    );

    public static final KeeperAbilityType SINGLE_HORIZONTAL_FAST = new KeeperAbilityType(
            null,
            EventKeyframeHandler.of(swingSound(8), hitFrame(12, true)),
            24, "sword_single_horizontal_fast",
            KeeperMeleeAbilityInstance::new, 1f
    );

    public static final KeeperAbilityType SINGLE_STAB = new KeeperAbilityType(
            null,
            EventKeyframeHandler.of(swingSound(7), hitFrame(11, true)),
            21, "sword_stab",
            KeeperMeleeAbilityInstance::new, 1f
    );

    public static final KeeperAbilityType LUNGE = new KeeperAbilityType(
            null,
            EventKeyframeHandler.of(swingSound(56)),
            76, "sword_lunge",
            KeeperLungeAbilityInstance::new,
            3f
    );

    public static final List<KeeperAbilityType> STANDARD_ATTACKS = List.of(
            DOUBLE_SLASH, SINGLE_UPWARD, SINGLE_HORIZONTAL, SINGLE_HORIZONTAL_FAST, SINGLE_STAB
    );

    public static final List<KeeperAbilityType> ALL_ATTACKS = List.of(
            DOUBLE_SLASH, SINGLE_UPWARD, SINGLE_HORIZONTAL, SINGLE_HORIZONTAL_FAST, SINGLE_STAB, LUNGE
    );

    private static EventKeyframe<KeeperEntity> swingSound(int tick) {
        return new SoundEventKeyframe<>(tick, SoundRegistry.KEEPER_SWING, 1f, .9f, 1.3f);
    }

    private static EventKeyframe<KeeperEntity> hitFrame(int tick, boolean allowCombo) {
        return new SimpleMeleeKeyframe<KeeperEntity>(tick) {
            @Override
            public void postHit(KeeperEntity mob, boolean hit, boolean blocking) {
                if (allowCombo && ((mob.getRandom().nextFloat() < .75f) || blocking)) {
                    mob.setQueuedAbility(STANDARD_ATTACKS.get(mob.getRandom().nextInt(STANDARD_ATTACKS.size())));
                }
            }
        }.lunge(new Vec3(0, 0, 0.55f))
                .iframes(0)
                .impactSound(new SoundEventKeyframe<>(0, SoundRegistry.KEEPER_SWORD_IMPACT, 1f, .9f, 1.3f));
    }
}
