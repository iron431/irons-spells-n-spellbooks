package io.redspace.skillcasting.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.network.ServerboundCastSelectedSkillPacket;
import io.redspace.skillcasting.network.ServerboundQuickCastSkillPacket;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static io.redspace.skillcasting.client.KeyMappings.*;

public final class ClientInputEvents {
    public static boolean hasReleasedSinceCasting;
    private static boolean showExpandedTooltip;

    private ClientInputEvents() {
    }

    @SubscribeEvent
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (SKILLBAR_SCROLL_MODIFIER_KEYMAP.isDown()) {
            int direction = Mth.clamp((int) event.getScrollDeltaY(), -1, 1);
            if (handleSkillBarScrollModifier(direction)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Modifier + scroll to cycle the selected skill on the bar.
     *
     * @return whether the scroll was consumed
     */
    public static boolean handleSkillBarScrollModifier(int direction) {
        SkillSelectionManager manager = SkillcastingData.get(Minecraft.getInstance().player).selectionManager();
        if (manager.getSkillCount() <= 0) {
            return false;
        }
        int skillCount = manager.getSkillCount();
        int scrollIndex = Mth.clamp(manager.getSelectionIndex(), 0, skillCount) - direction;
        int selectedIndex = (Mth.clamp(scrollIndex, -1, skillCount + 1) + skillCount) % skillCount;
        manager.makeSelection(selectedIndex);
        return true;
    }

    @SubscribeEvent
    public static void onUseInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isUseItem()) {
            if (ClientSkillCastHelper.shouldSuppressRightClicks()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        } else if (event.isAttack()) {
            if (SkillcastingData.get(Minecraft.getInstance().player).isCasting()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        handleKeybinds();
    }

    private static boolean wasSkillWheelDown;

    private static void handleKeybinds() {
        while (CAST_SELECTED_SKILL_KEYMAP.consume()) {
            PacketDistributor.sendToServer(new ServerboundCastSelectedSkillPacket());
        }
        while (SKILL_WHEEL_KEYMAP.consume()) {
            if (!wasSkillWheelDown) {
                SkillWheelOverlay.instance.open();
            }
        }
        handleSkillWheelRelease();
        while (SKILL_WHEEL_TOGGLE_KEYMAP.consume()) {
            if (SkillWheelOverlay.instance.active) {
                SkillWheelOverlay.instance.close();
            } else {
                SkillWheelOverlay.instance.open();
            }
        }
        for (int i = 0; i < QUICK_CAST_MAPPINGS.size(); i++) {
            if (QUICK_CAST_MAPPINGS.get(i).consume()) {
                PacketDistributor.sendToServer(new ServerboundQuickCastSkillPacket(i));
                break;
            }
        }
        updateShowExpandedTooltip();
        handleUseRelease();
    }

    private static void handleSkillWheelRelease() {
        boolean isDown = SKILL_WHEEL_KEYMAP.isDown();
        if (wasSkillWheelDown && !isDown && SkillWheelOverlay.instance.active) {
            SkillWheelOverlay.instance.close();
        }
        wasSkillWheelDown = isDown;
    }

    private static void updateShowExpandedTooltip() {
        showExpandedTooltip = isKeyboardMouseInputDown(Minecraft.getInstance().options.keyShift.getDefaultKey());
    }

    private static boolean wasUseDown;

    private static void handleUseRelease() {
        boolean isDown = Minecraft.getInstance().options.keyUse.isDown();
        if (wasUseDown && !isDown) {
            ClientSkillCastHelper.setSuppressRightClicks(false);
            hasReleasedSinceCasting = true;
        }
        wasUseDown = isDown;
    }

    public static boolean isShowExpandedTooltip() {
        return showExpandedTooltip;
    }

    public static boolean isUseKeyDown() {
        return isKeyboardMouseInputDown(Minecraft.getInstance().options.keyUse.getKey());
    }

    private static boolean isKeyboardMouseInputDown(InputConstants.Key key) {
        int keyValue = key.getValue();
        long windowPointer = Minecraft.getInstance().getWindow().getWindow();
        if (key.getType() == InputConstants.Type.KEYSYM) {
            return GLFW.glfwGetKey(windowPointer, keyValue) > 0;
        } else if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(windowPointer, keyValue) > 0;
        }
        return false;
    }
}
