package io.redspace.ironsspellbooks.gui.overlays;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.joml.Vector2ic;
import org.joml.Vector4i;

import java.util.List;
import java.util.Optional;


public class ScreenTooltipOverlay implements LayeredDraw.Layer {
    public static final ScreenTooltipOverlay instance = new ScreenTooltipOverlay();

    private record RenderInfo(List<Component> tooltip, ClientTooltipPositioner positioner, Optional<Vector4i> colors) {
    }

    RenderInfo toRender = null;

    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        if (Minecraft.getInstance().options.hideGui || toRender == null) {
            return;
        }

        // manually render tooltip for extra control (mainly color/opacity)
        //guiHelper.renderTooltip(Minecraft.getInstance().font, Language.getInstance().getVisualOrder(toRender.tooltip.stream().map(ScreenTooltipOverlay::cast).toList()), toRender.positioner, 0, 0);
        var pose = guiHelper.pose();

        var font = Minecraft.getInstance().font;
        var components = Language.getInstance().getVisualOrder(toRender.tooltip.stream().map(ScreenTooltipOverlay::cast).toList()).stream().map(ClientTextTooltip::new).toList();
        int maxTextWidth = 0;
        int totalHeight = components.size() == 1 ? -2 : 0;

        for (ClientTooltipComponent clienttooltipcomponent : components) {
            int k = clienttooltipcomponent.getWidth(font);
            if (k > maxTextWidth) {
                maxTextWidth = k;
            }

            totalHeight += clienttooltipcomponent.getHeight();
        }

        Vector2ic vector2ic = toRender.positioner.positionTooltip(guiHelper.guiWidth(), guiHelper.guiHeight(), 0, 0, maxTextWidth, totalHeight);
        int x = vector2ic.x();
        int y = vector2ic.y();
        pose.pushPose();
        //0xf0100010, 0x505000FF, 0x5028007f
        var bgColor1 = toRender.colors.map(Vector4i::x).orElse(0x90100010);
        var bgColor2 = toRender.colors.map(Vector4i::y).orElse(0x90100010);
        var edgeColor1 = toRender.colors.map(Vector4i::z).orElse(0x705000FF);
        var edgeColor2 = toRender.colors.map(Vector4i::w).orElse(0x7028007f);
        TooltipRenderUtil.renderTooltipBackground(guiHelper, x, y, maxTextWidth, totalHeight, 400,
                bgColor1,
                bgColor2,
                edgeColor1,
                edgeColor2);
        guiHelper.flush();
        pose.translate(0.0F, 0.0F, 400.0F);
        int lineY = y;

        for (int l1 = 0; l1 < components.size(); l1++) {
            ClientTooltipComponent clienttooltipcomponent1 = components.get(l1);
            clienttooltipcomponent1.renderText(font, x, lineY, pose.last().pose(), guiHelper.bufferSource());
            lineY += clienttooltipcomponent1.getHeight() + (l1 == 0 ? 2 : 0);
        }
        pose.popPose();


        toRender = null;
    }

    private static FormattedText cast(Component component) {
        return component;
    }

    public static void renderTooltip(List<Component> tooltip, ClientTooltipPositioner positioner) {
        instance.toRender = new RenderInfo(tooltip, positioner, Optional.empty());
    }

    /**
     * Color in order of bgColor1
     * bgColor2
     * edgeColor1
     * edgeColor2
     */
    public static void renderTooltip(List<Component> tooltip, ClientTooltipPositioner positioner, Vector4i colors) {
        instance.toRender = new RenderInfo(tooltip, positioner, Optional.of(colors));
    }
}
