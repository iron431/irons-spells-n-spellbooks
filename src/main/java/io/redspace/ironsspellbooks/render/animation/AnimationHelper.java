package io.redspace.ironsspellbooks.render.animation;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AnimationHelper {
    public static ResourceLocation PLAYER_ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animation");

    public static void initializePlayerAnimationFactory() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(PLAYER_ANIMATION_RESOURCE,
                42, IronPlayerAnimationData::new);
    }

    public static void cancelPlayerAnimation(AbstractClientPlayer player) {
        var playerAnimationData = (IronPlayerAnimationData<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(PLAYER_ANIMATION_RESOURCE);
        if (playerAnimationData != null) {
            playerAnimationData.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(4, Ease.INOUTSINE), null, false);
            playerAnimationData.adjustmentModifier.setupFade(3, 3);
            playerAnimationData.setSpeed(1f);
        }
    }

    public static void animatePlayerStart(Player player, ResourceLocation resourceLocation) {
        animatePlayerStart(player, resourceLocation, 1f);
    }

    public static void animatePlayerStart(Player player, ResourceLocation resourceLocation, float speed) {
        try {
            var rawanimation = PlayerAnimationRegistry.getAnimation(resourceLocation);
            if (rawanimation instanceof KeyframeAnimation keyframeAnimation) {
                var copy = keyframeAnimation.mutableCopy();
                copy.isEasingBefore = true;
                var fadein = 2;
                var animation = new KeyframeAnimationPlayer(copy.build());
                var playerAnimationData = (IronPlayerAnimationData<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(PLAYER_ANIMATION_RESOURCE);
                playerAnimationData.adjustmentModifier.setupFade(animation.getData().endTick, 3);
                playerAnimationData.setSpeed(speed);
                var armsFlag = ClientConfigs.SHOW_FIRST_PERSON_ARMS.get();
                var itemsFlag = ClientConfigs.SHOW_FIRST_PERSON_ITEMS.get();
                if (armsFlag || itemsFlag) {
                    animation.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
                    animation.setFirstPersonConfiguration(new FirstPersonConfiguration(armsFlag, armsFlag, itemsFlag, itemsFlag));
                } else {
                    animation.setFirstPersonMode(FirstPersonMode.DISABLED);
                }
                playerAnimationData.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadein, Ease.INOUTSINE), animation, true);
            }
        } catch (Exception e) {
            IronsSpellbooks.LOGGER.error("Failed to play player animation: {}", e.getMessage());
        }
    }
}
