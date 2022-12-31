package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.spells.SpellType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;

public class ClientPlayerEvents {

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END) {
            ClientMagicData.getCooldowns().tick(1);
        }
    }

    //https://github.com/TeamPneumatic/pnc-repressurized/commit/f901dae5180f378548c288da8f74373e1db631eb
    //https://www.tabnine.com/code/java/classes/net.minecraftforge.client.event.RenderPlayerEvent$Post
    //https://www.programcreek.com/java-api-examples/?code=TeamWizardry%2FWizardry%2FWizardry-master%2Fsrc%2Fmain%2Fjava%2Fcom%2Fteamwizardry%2Fwizardry%2Fclient%2Fcosmetics%2FCapeHandler.java
    //https://forums.minecraftforge.net/topic/87214-1152-rotate-player-arms-and-editremove-animation-when-holding-an-item/

    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        //TestMod.LOGGER.info("RenderPlayerEvent.Post");
        if (ClientMagicData.getCooldowns().isOnCooldown(SpellType.FIREBALL_SPELL)) {
            var player = event.getPlayer();
            var render = event.getRenderer();
            var model = render.getModel();

            var skinLocation = ((AbstractClientPlayer) player).getSkinTextureLocation();

            model.leftArm.z = 0.0F;
            model.leftArm.x = 5.0F;
            model.leftArm.xRot = Mth.cos(model.leftArm.xRot * 0.6662F) * 0.25F;
            model.leftArm.zRot = -2.3561945F;
            model.leftArm.yRot = 0.0F;

            model.rightArm.x = -5.0F;
            model.rightArm.xRot = Mth.cos(model.rightArm.xRot * 0.6662F) * 0.25F;
            model.rightArm.zRot = 2.3561945F;
            model.rightArm.yRot = 0.0F;

            model.leftArm.render(event.getPoseStack(), event.getMultiBufferSource().getBuffer(model.renderType(skinLocation)), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
            model.rightArm.render(event.getPoseStack(), event.getMultiBufferSource().getBuffer(model.renderType(skinLocation)), event.getPackedLight(), OverlayTexture.NO_OVERLAY);

        }

    }


}