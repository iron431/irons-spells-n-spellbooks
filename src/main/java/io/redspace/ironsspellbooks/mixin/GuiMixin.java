package io.redspace.ironsspellbooks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderExperienceBar", at = @At(value = "HEAD"), cancellable = true)
    public void renderExperienceBar(GuiGraphics guiGraphics, int pXPos, CallbackInfo ci) {
        //TODO: 1.20 port mana bar implementation
        if (ClientConfigs.MANA_BAR_ANCHOR.get() == ManaBarOverlay.Anchor.XP && Minecraft.getInstance().player != null && /*ManaBarOverlay.shouldShowManaBar(Minecraft.getInstance().player)*/false)
            ci.cancel();
    }
}
