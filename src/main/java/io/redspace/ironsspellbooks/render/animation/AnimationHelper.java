package io.redspace.ironsspellbooks.render.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static io.redspace.ironsspellbooks.config.ClientConfigs.SHOW_FIRST_PERSON_ARMS;
import static io.redspace.ironsspellbooks.config.ClientConfigs.SHOW_FIRST_PERSON_ITEMS;

public class AnimationHelper {
    public static void initializePlayerAnimationFactory() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                SpellAnimations.ANIMATION_RESOURCE,
                42,
                (player) -> {
                    var animation = new ModifierLayer<>();
                    IronsAdjustmentModifier.INSTANCE = new IronsAdjustmentModifier((partName, partialTick) -> {
                        boolean handleHead = animation.getAnimation() != null && !animation.getAnimation().get3DTransform("head", TransformType.ROTATION, 0.5f, Vec3f.ZERO).equals(Vec3f.ZERO);
                        switch (partName) {
                            case "head" -> {
                                if (handleHead) {
                                    return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(0, Mth.lerp(partialTick, (player.yHeadRotO - player.yBodyRotO), (player.yHeadRot - player.yBodyRot)) * Mth.DEG_TO_RAD, 0), Vec3f.ZERO));
                                } else {
                                    return Optional.empty();
                                }
                            }
                            case "rightArm", "leftArm" -> {
                                float x = Mth.wrapDegrees(Mth.lerp(partialTick, player.xRotO, player.getXRot()) * 0.65f);
                                float y = Mth.wrapDegrees(Mth.lerp(partialTick, (player.yHeadRotO - player.yBodyRotO), (player.yHeadRot - player.yBodyRot)) * 0.65f);
                                Vec3f posAdjustment = Vec3f.ZERO;
                                if (animation.getAnimation() != null) {
                                    Vec3f currentPos = animation.getAnimation().get3DTransform(partName, TransformType.POSITION, partialTick, Vec3f.ZERO);
                                    Vec3 rotatedPos = new Vec3(currentPos.getX(), currentPos.getY(), currentPos.getZ()).xRot(-x * Mth.DEG_TO_RAD).yRot(y * Mth.DEG_TO_RAD);
                                    posAdjustment = new Vec3f((float) (rotatedPos.x - currentPos.getX()), (float) (rotatedPos.y - currentPos.getY()), (float) (rotatedPos.z - currentPos.getZ()));
                                }
                                return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(x * Mth.DEG_TO_RAD, y * Mth.DEG_TO_RAD, 0), posAdjustment));
                            }
                            default -> {
                                return Optional.empty();
                            }
                        }
                    });
                    animation.addModifier(IronsAdjustmentModifier.INSTANCE, 0);
                    animation.addModifierLast(new MirrorModifier() {
                        @Override
                        public boolean isEnabled() {
                            return ClientMagicData.getSyncedSpellData(player).getCastingEquipmentSlot().equals(SpellSelectionManager.OFFHAND) ^ player.getMainArm() == HumanoidArm.LEFT;
                        }
                    });

                    return animation;
                });
    }

    public static void cancelPlayerAnimation(AbstractClientPlayer player) {
        var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(SpellAnimations.ANIMATION_RESOURCE);
        if (animation != null) {
            animation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(4, Ease.INOUTSINE), null, false);
            IronsAdjustmentModifier.INSTANCE.fadeOut(5);
        }
    }

    public static void animatePlayerStart(Player player, ResourceLocation resourceLocation) {
        var rawanimation = PlayerAnimationRegistry.getAnimation(resourceLocation);
        if (rawanimation instanceof KeyframeAnimation keyframeAnimation) {
            //noinspection unchecked
            var playerAnimationData = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(SpellAnimations.ANIMATION_RESOURCE);
            if (playerAnimationData != null) {
                var animation = new KeyframeAnimationPlayer(keyframeAnimation) {
//                    @Override
//                    public void stop() {
//                        playerAnimationData.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), null, false);
//                        IronsAdjustmentModifier.INSTANCE.fadeOut(3);
//                    }

                    @Override
                    public void tick() {
                        if (getCurrentTick() == getStopTick() - 2) {
                            IronsAdjustmentModifier.INSTANCE.fadeOut(3);
                        }
                        super.tick();
                    }
                };
                var armsFlag = SHOW_FIRST_PERSON_ARMS.get();
                var itemsFlag = SHOW_FIRST_PERSON_ITEMS.get();
                if (armsFlag || itemsFlag) {
                    animation.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
                    animation.setFirstPersonConfiguration(new FirstPersonConfiguration(armsFlag, armsFlag, itemsFlag, itemsFlag));
                } else {
                    animation.setFirstPersonMode(FirstPersonMode.DISABLED);
                }
                playerAnimationData.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), animation, true);
            }
        }
    }
}
